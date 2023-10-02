package com.techelevator.tenmo.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transfer {
    private int transferId;
    private int senderAccountId;
    private int receiverAccountId;
    private BigDecimal transferAmount;
    private String status;
    private Timestamp transferTimestamp;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public Timestamp getTransferTimestamp() {
        return transferTimestamp;
    }

    public void setTransferTimestamp(Timestamp transferTimestamp) {
        this.transferTimestamp = transferTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(int senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(int receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }
}
