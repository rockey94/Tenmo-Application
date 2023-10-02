package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "api/transfers")
public class TransferController {

    private final UserDao userDao;
    private final TransferDao transferDao;
    private final AccountDao accountDao;

    public TransferController(UserDao userDao, TransferDao transferDao, AccountDao accountDao) {
        this.userDao = userDao;
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/{transferId}", method = RequestMethod.GET)
    public TransferResponse get(@PathVariable int transferId) {
        Transfer transfer = transferDao.getTransferById(transferId);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }
        return createTransferResponse(transfer);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<TransferResponse> getUserTransferHistory(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());

        if (userId == -1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Account account = accountDao.getAccountByUserId(userId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        int accountId = account.getAccountId();
        List<Transfer> transfers = transferDao.getTransfersByAccountId(accountId);
        List<TransferResponse> transferResponses = new ArrayList<>();
        for (Transfer transfer : transfers) {


            transferResponses.add(createTransferResponse(transfer));
        }
        return transferResponses;
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<TransferUsersResponse> getUsersThatCanBeTransferredTo(Principal principal) {
        List<TransferUsersResponse> usersToTransferTo = new ArrayList<>();

        String username = principal.getName();
        List<User> users = userDao.findAll();
        for (User user : users) {
            if (!user.getUsername().equals(username)) {
                usersToTransferTo.add(new TransferUsersResponse(user.getUsername()));
            }
        }

        return usersToTransferTo;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public TransferResponse addTransfer(@Valid @RequestBody TransferDTO transferDTO, Principal principal) {
        // TODO is not found an appropriate response for a post request? ASK JEREMY if should be bad request instead

        int receiverUserId = userDao.findIdByUsername(transferDTO.getUsername());
        if (receiverUserId == -1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver account not found");
        }

        int senderUserId = userDao.findIdByUsername(principal.getName());
        if (senderUserId == -1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender account not found");
        }

        if (senderUserId == receiverUserId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An account cannot transfer to itself");
        }

        Account senderAccount = accountDao.getAccountByUserId(senderUserId);
        if (senderAccount == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender account not found");
        }

        Account receiverAccount = accountDao.getAccountByUserId(receiverUserId);
        if (receiverAccount == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver account not found");
        }

        BigDecimal senderAccountBalance = senderAccount.getBalance();
        BigDecimal transferAmount = transferDTO.getTransferAmount();
        if (senderAccountBalance.compareTo(transferAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender account, insufficient balance");
        }
        if (transferAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be a positive amount");
        }

        Transfer transferToCreate = new Transfer();
        transferToCreate.setTransferAmount(transferAmount);
        transferToCreate.setStatus("Approved");
        transferToCreate.setSenderAccountId(senderAccount.getAccountId());
        transferToCreate.setReceiverAccountId(receiverAccount.getAccountId());
        Transfer createdTransfer = transferDao.createTransfer(transferToCreate);

        senderAccount.setBalance(senderAccountBalance.subtract(transferAmount));

        BigDecimal receiverAccountBalance = receiverAccount.getBalance();
        receiverAccount.setBalance(receiverAccountBalance.add(transferAmount));

        // TODO exception handling - ASK JEREMY how a transaction would be applied here
        accountDao.updateAccount(senderAccount);
        accountDao.updateAccount(receiverAccount);

        return new TransferResponse(createdTransfer.getTransferId(), transferAmount, principal.getName(), transferDTO.getUsername());
    }

    private TransferResponse createTransferResponse(Transfer transfer) {
        String senderUsername = userDao.findUsernameByAccountId(transfer.getSenderAccountId());
        if (senderUsername == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        String receiverUsername = userDao.findUsernameByAccountId(transfer.getReceiverAccountId());
        if (receiverUsername == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        TransferResponse transferResponse = new TransferResponse();

        transferResponse.setFrom(senderUsername);
        transferResponse.setTo(receiverUsername);
        transferResponse.setTransferAmount(transfer.getTransferAmount());
        transferResponse.setTransferId(transfer.getTransferId());
        return transferResponse;
    }


    // TODO: ASK JEREMY if there is a better way to handle this problem (returning too much info)

    static class TransferUsersResponse {
        String username;

        public TransferUsersResponse(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    static class TransferResponse {
        int transferId;
        BigDecimal transferAmount;
        String from;
        String to;

        public TransferResponse(int transferId, BigDecimal transferAmount, String from, String to) {
            this.transferId = transferId;
            this.transferAmount = transferAmount;
            this.from = from;
            this.to = to;
        }

        public TransferResponse() {
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public int getTransferId() {
            return transferId;
        }

        public BigDecimal getTransferAmount() {
            return transferAmount;
        }

        public void setTransferId(int transferId) {
            this.transferId = transferId;
        }

        public void setTransferAmount(BigDecimal transferAmount) {
            this.transferAmount = transferAmount;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
