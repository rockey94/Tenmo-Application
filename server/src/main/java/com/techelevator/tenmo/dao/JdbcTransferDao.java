package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import exception.DaoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Transfer getTransferById(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, sending_account_id, " +
                "receiving_account_id, amount, transfer_timestamp, " +
                "status FROM transfer WHERE transfer_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfer;

    }

    @Override
    public List<Transfer> getTransfersByAccountId(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, sending_account_id, " +
                "receiving_account_id, amount, transfer_timestamp, " +
                "status FROM transfer WHERE sending_account_id = ? OR receiving_account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return transfers;
    }

    @Override
    public Transfer createTransfer(Transfer transfer) {
        Transfer newTransfer;
        String sql = "INSERT INTO transfer (sending_account_id, " +
                "receiving_account_id, amount, status) VALUES (?, ?, ?, ?)" +
                " RETURNING transfer_id;";
        try {
            Integer transferId = jdbcTemplate.queryForObject(sql, Integer.class, transfer.getSenderAccountId(), transfer.getReceiverAccountId(), transfer.getTransferAmount(), transfer.getStatus());
            newTransfer = getTransferById(transferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newTransfer;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setSenderAccountId(rowSet.getInt("sending_account_id"));
        transfer.setReceiverAccountId(rowSet.getInt("receiving_account_id"));
        transfer.setTransferAmount(rowSet.getBigDecimal("amount"));
        transfer.setTransferTimestamp(rowSet.getTimestamp("transfer_timestamp"));
        transfer.setStatus(rowSet.getString("status"));
        return transfer;
    }
}
