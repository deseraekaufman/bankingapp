package com.techelevator.tenmo.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Transfer {

    @NotNull(message = "The field idFrom is required.")
    private int idFrom;
    //private String fromName;
    //private String toName;
    @NotNull (message = "The field idTo is required.")
    private int idTo;
    @NotNull (message = "The field amount is required.")
    private double amount;
    private int transferTypeId;
    private int transferStatusId;
    private int transferId;

//    public String getFromName() {
//        return fromName;
//    }
//
//    public void setFromName(String fromName) {
//        this.fromName = fromName;
//    }
//
//    public String getToName() {
//        return toName;
//    }
//
//    public void setToName(String toName) {
//        this.toName = toName;
//    }

    public int getTransferTypeId() {
        return transferTypeId;
    }

    public void setTransferTypeId(int transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public int getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getIdFrom() {
        return idFrom;
    }

    public int getIdTo() {
        return idTo;
    }

    public double getAmount() {
        return amount;
    }

    public void setIdFrom(int idFrom) {
        this.idFrom = idFrom;
    }

    public void setIdTo(int idTo) {
        this.idTo = idTo;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


}
