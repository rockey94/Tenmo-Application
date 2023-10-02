package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

public interface AccountDao {
    Account getAccountById(int accountId);

    Account getAccountByUserId(int userId);

    Account createAccount(Account account);

    Account updateAccount(Account account);

}
