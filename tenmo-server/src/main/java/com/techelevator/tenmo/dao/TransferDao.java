package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface TransferDao {

    void create (Transfer transfer);

    List<TransferDTO> getUserTransfer(int userId);

    TransferDTO getTransferById(int transferId, int userId);

    void pending (Transfer transfer);

    void updatePending (Transfer transfer);

    void adjustBalance (Transfer transfer);

    Transfer getTransferByTransferId (int id);

    List<TransferDTO> getPendingTransfer(int userId);

}
