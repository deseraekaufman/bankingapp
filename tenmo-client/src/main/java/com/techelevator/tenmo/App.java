package com.techelevator.tenmo;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.util.BasicLogger;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    private TransferService transferService;



    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            transferService = new TransferService(currentUser);
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		double balance = 0;
        try {
            balance = transferService.getBalance();
        }catch (RestClientException e) {
           BasicLogger.log(e.getMessage());
        }
        System.out.println("Your current account balance is: $" + balance);
	}



	private void viewTransferHistory() {
 TransferDTO[] transferList = null;
        try {
             transferList = transferService.viewTransferHistory();
        } catch (RestClientException e) {
            BasicLogger.log(e.getMessage());
        }
        System.out.println("ID  \t|\t  From/To   |\t   Amount");
        for (TransferDTO tf : transferList){
            /*
            If transfer is from current user print "To: Destination User Name"
            If transfer is to current user print "From: Requested User Name"
            */
            if (tf.getUserFrom() == null) {
                System.out.println(tf.getTransferID() + "\t | \t" + "To: " + tf.getUserTo() + "\t | \t$" + tf.getTransferAmount());
            } else {
                System.out.println(tf.getTransferID() + "\t | \t" + "From: " + tf.getUserFrom() + "\t | \t$" + tf.getTransferAmount());
            }


        }
	    String print = "Please enter transfer ID to view details (0 to cancel):";

        boolean isCorrect = true;
        int choice = 0;
        TransferDTO transfer = null;
        while(isCorrect) {

            try {
                 choice = consoleService.promptForInt(print);
                transfer = transferService.viewTransferById(choice);
            } catch (RestClientException e) {
                BasicLogger.log(e.getMessage());
            }
            for(TransferDTO transferDTO : transferList){
                if(transferDTO.getTransferID()==choice){
                    isCorrect = false;
                    break;
                }
            }
        }
        System.out.println("Id: "+ transfer.getTransferID() + "\nTo: " + transfer.getUserTo() + "\nFrom: " +  transfer.getUserFrom() + "\nType: " +
                transfer.getTransferType() + "\nStatus: " + transfer.getTransferStatus() + "\nAmount: $" + transfer.getTransferAmount());
    }


	private void viewPendingRequests() {
        // TODO Auto-generated method stub
        TransferDTO[] transferList = null;
        try {
            transferList = transferService.viewPendingRequests();
        } catch (RestClientException e) {
            BasicLogger.log(e.getMessage());
        }
        System.out.println("ID  \t|\t  From/To   |\t   Amount");
        for (TransferDTO tf : transferList) {
            /*
            If transfer is from current user print "To: Destination User Name"
            If transfer is to current user print "From: Requested User Name"
            */
            if (tf.getUserFrom() == null) {
                System.out.println(tf.getTransferID() + "\t | \t" + "To: " + tf.getUserTo() + "\t | \t$" + tf.getTransferAmount());
            } else {
                System.out.println(tf.getTransferID() + "\t | \t" + "From: " + tf.getUserFrom() + "\t | \t$" + tf.getTransferAmount());
            }


        }
        String print = "Please enter transfer ID to approve/reject (0 to cancel):";

        boolean isCorrect = true;
        int choice = 0;
        TransferDTO transfer = null;
        while (isCorrect) {

            try {
                choice = consoleService.promptForInt(print);
                transfer = transferService.viewTransferById(choice);
            } catch (RestClientException e) {
                BasicLogger.log(e.getMessage());
            }
            for (TransferDTO transferDTO : transferList) {
                if (transferDTO.getTransferID() == choice) {
                    isCorrect = false;
                    break;
                }
            }
        }
        String str = "\t1: Approve \n\t2: Reject \n\t0: Don't approve or reject \n\tPlease choose an option:";

        int selection = consoleService.promptForInt(str);
        Transfer tf = new Transfer();
        tf.setTransferId(transfer.getTransferID());
        tf.setAmount(transfer.getTransferAmount());
        tf.setTransferStatusId(selection);

        if (selection != 0) {
            transferService.updateTransfer(selection, tf);
        }
    }


	private void sendBucks() {
        User[] users = transferService.getUsers();
        String print = "\nUser ID | User Name \n";

        for (User user : users) {
            print += user.getId() + " | " + user.getUsername() + "\n";
        }

        System.out.println(print);

        print = "Enter User ID that you want to send TE bucks to (0 to cancel): ";
        boolean isCorrect = true;
        int choice = 0;
        while(isCorrect) {
            choice = consoleService.promptForInt(print);

            for (User user : users) {


                if (user.getId() == choice) {
                    isCorrect = false;
                    break;
                }
            }
        }
        print = "How many TE bucks would you like to send: ";
        double amount = 0;
        while(amount <=0) {
            amount = consoleService.promptForBigDecimal(print).doubleValue();
        }
        try {
            transferService.createTransfer(choice, amount);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

	}

	private void requestBucks() {
        User[] users = transferService.getUsers();
        String print = "\nUser ID | User Name \n";

        for (User user : users) {
            print += user.getId() + " | " + user.getUsername() + "\n";
        }

        System.out.println(print);

        print = "Enter User ID that you want to request TE bucks to (0 to cancel): ";
        boolean isCorrect = true;
        int choice = 0;
        while(isCorrect) {
            choice = consoleService.promptForInt(print);

            for (User user : users) {


                if (user.getId() == choice) {
                    isCorrect = false;
                    break;
                }
            }
        }
        print = "How many TE bucks would you like to request: ";
        double amount = 0;
        while(amount <=0) {
            amount = consoleService.promptForBigDecimal(print).doubleValue();
        }
        try {
            transferService.createRequest(choice, amount);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

		
	}

}
