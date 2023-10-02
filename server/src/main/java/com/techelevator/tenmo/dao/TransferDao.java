package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {
    Transfer getTransferById(int transferId);

    List<Transfer> getTransfersByAccountId(int accountId);

    Transfer createTransfer(Transfer transfer);
}
