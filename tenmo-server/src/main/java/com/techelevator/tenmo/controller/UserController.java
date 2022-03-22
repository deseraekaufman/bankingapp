package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.ls.LSOutput;

import javax.sql.DataSource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class UserController {
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransferDao transferDao;

    public UserController(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new JdbcUserDao(jdbcTemplate);
        this.transferDao = new JdbcTransferDao(jdbcTemplate);
    }

    @RequestMapping(path = "/user/", method = RequestMethod.GET)
    public @ResponseBody Double getBalance(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());
        return userDao.getUserBalance(userId);
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public List<User> userList(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());
        return userDao.findAll(userId);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public void createTransfer(@Valid @RequestBody Transfer transfer, Principal principal) {

        int userPrinciple = userDao.findIdByUsername(principal.getName());
        if (userPrinciple!=transfer.getIdFrom() && userPrinciple!=transfer.getIdTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        double userBalance = userDao.getUserBalance(transfer.getIdFrom());
        if (transfer.getAmount()>userBalance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
        }
        if (transfer.getIdTo() == transfer.getIdFrom()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User can't request money from themself.");
        }
        transferDao.create(transfer);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.GET)
    public TransferDTO[] getUserTransfers(Principal principal){
        int userId = userDao.findIdByUsername(principal.getName());
        List<TransferDTO> transferList = transferDao.getUserTransfer(userId);
        return transferList.toArray(new TransferDTO[0]);
    }

    @RequestMapping(path = "/transfer/{id}", method = RequestMethod.GET)
    public TransferDTO getTransferById(@PathVariable int id, Principal principal){
        int userId = userDao.findIdByUsername(principal.getName());
        TransferDTO transfer = transferDao.getTransferById(id,userId);
        return transfer;
    }

    @RequestMapping(path = "/transfer/pending", method = RequestMethod.POST)
    public void createPending(@Valid @RequestBody Transfer transfer, Principal principal) {

        int userPrinciple = userDao.findIdByUsername(principal.getName());
        if (userPrinciple != transfer.getIdFrom() && userPrinciple != transfer.getIdTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        //double userBalance = userDao.getUserBalance(transfer.getIdFrom());
        //if (transfer.getAmount() > userBalance) {
          //  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
        //}
        if (transfer.getIdTo() == transfer.getIdFrom()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User can't request money from themself.");
        }
        transferDao.pending(transfer);
    }

    @RequestMapping(path = "/transfer/pending/{id}", method = RequestMethod.PUT)
    public void updatePending(@RequestBody Transfer transfer1, Principal principal, @PathVariable int id) {
        Transfer transfer = transferDao.getTransferByTransferId(transfer1.getTransferId());
        int userPrinciple = userDao.findIdByUsername(principal.getName());
//        if (userPrinciple != transfer.getIdFrom()-1000 && userPrinciple != transfer.getIdTo()-1000) {
//           throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//
//        double userBalance = userDao.getUserBalance(transfer.getIdFrom()-1000);
//        if (transfer.getAmount() > userBalance) {
//          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
//        }
//        if (transfer.getIdTo() == transfer.getIdFrom()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User can't request money from themself.");
//        }
        transferDao.updatePending(transfer);
        if (transfer.getTransferStatusId() == 1){
            transferDao.adjustBalance(transfer);
        }
    }

    @RequestMapping(path = "/transfer/pending", method = RequestMethod.GET)
    public TransferDTO[] getPendingTransfer(Principal principal){
        int userId = userDao.findIdByUsername(principal.getName());
        List<TransferDTO> transferList = transferDao.getPendingTransfer(userId);
        return transferList.toArray(new TransferDTO[0]);
    }

    /*


       9. As an authenticated user of the system, I need to be able to either approve or reject a Request Transfer.
            1. I can't "approve" a given Request Transfer for more TE Bucks than I have in my account.
            2. The Request Transfer status is *Approved* if I approve, or *Rejected* if I reject the request.
            3. If the transfer is approved, the requester's account balance is increased by the amount of the request.
            4. If the transfer is approved, the requestee's account balance is decreased by the amount of the request.
            5. If the transfer is rejected, no account balance changes.

     */
}
