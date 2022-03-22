package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class TransferService {

    private AuthenticatedUser currentUser;

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();


    public TransferService(AuthenticatedUser currentUser) {
    this.currentUser = currentUser;
}

    public Double getBalance(){
        ResponseEntity<Double> response = restTemplate.exchange(API_BASE_URL + "user/" , HttpMethod.GET, makeAuthEntity(), Double.class);
        return response.getBody();
    }

    public User[] getUsers() {
        ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "user", HttpMethod.GET, makeAuthEntity(), User[].class);
        return response.getBody();
    }

    public void createTransfer(int userID, double amount) {
        Transfer transfer = new Transfer();
        transfer.setIdTo(userID);
        transfer.setAmount(amount);
        transfer.setIdFrom(currentUser.getUser().getId().intValue());

        restTemplate.postForObject(API_BASE_URL + "/transfer", makeTransferEntity(transfer), Transfer.class);

    }

    public TransferDTO[] viewTransferHistory(){
        ResponseEntity<TransferDTO[]> response = restTemplate.exchange(API_BASE_URL + "transfer", HttpMethod.GET, makeAuthEntity(), TransferDTO[].class);
        return response.getBody();
    }

    public TransferDTO viewTransferById(int id){
        ResponseEntity<TransferDTO> response = restTemplate.exchange(API_BASE_URL + "transfer/" + id, HttpMethod.GET, makeAuthEntity(), TransferDTO.class);
        return response.getBody();
    }

    public TransferDTO[] viewPendingRequests() {
        ResponseEntity<TransferDTO[]> response = restTemplate.exchange(API_BASE_URL + "transfer/pending", HttpMethod.GET, makeAuthEntity(), TransferDTO[].class);
        return response.getBody();

    }

    public void createRequest(int userID, double amount) {
        Transfer transfer = new Transfer();
        transfer.setIdFrom(userID);
        transfer.setAmount(amount);
        transfer.setIdTo(currentUser.getUser().getId().intValue());

        restTemplate.postForObject(API_BASE_URL + "/transfer/pending", makeTransferEntity(transfer), Transfer.class);

    }
    public void updateTransfer(int status, Transfer transfer){
        String str = API_BASE_URL + "/transfer/pending/" + transfer.getTransferId() + "?status=" + status;
        restTemplate.put(str, makeTransferEntity(transfer));
    }

    private HttpEntity<Transfer> makeTransferEntity (Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }



}
