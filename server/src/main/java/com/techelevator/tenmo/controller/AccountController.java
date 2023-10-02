package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import exception.DaoException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/account")
public class AccountController {
    private final AccountDao accountDao;
    private final UserDao userDao;

    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @RequestMapping(method = RequestMethod.GET)
    public AccountResponse get(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());

        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Account account = accountDao.getAccountByUserId(userId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found");
        }
        return new AccountResponse(principal.getName(), account.getBalance());
    }

    static class AccountResponse {
        String username;
        BigDecimal balance;

        public AccountResponse(String username, BigDecimal balance) {
            this.username = username;
            this.balance = balance;
        }

        public String getUsername() {
            return username;
        }

        public BigDecimal getBalance() {
            return balance;
        }

    }
}
