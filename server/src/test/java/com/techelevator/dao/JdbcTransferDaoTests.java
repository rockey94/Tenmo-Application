package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class JdbcTransferDaoTests extends BaseDaoTests {
    private JdbcTransferDao sut;
    public static Transfer TRANSFER_1;
    public static Transfer TRANSFER_2;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
    }

    @Before
    public void createTransfers() {
        TRANSFER_1 = new Transfer();
        TRANSFER_1.setTransferId(3001);
        TRANSFER_1.setSenderAccountId(2001);
        TRANSFER_1.setReceiverAccountId(2002);
        TRANSFER_1.setTransferAmount(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP));
        TRANSFER_1.setTransferTimestamp(Timestamp.valueOf(LocalDateTime.of(2018, 3, 3, 3, 3, 3)));
        TRANSFER_1.setStatus("Approved");

        TRANSFER_2 = new Transfer();
        TRANSFER_2.setTransferId(3002);
        TRANSFER_2.setSenderAccountId(2002);
        TRANSFER_2.setReceiverAccountId(2001);
        TRANSFER_2.setTransferAmount(BigDecimal.valueOf(500).setScale(2, RoundingMode.HALF_UP));
        TRANSFER_2.setTransferTimestamp(Timestamp.valueOf(LocalDateTime.of(2018, 3, 3, 3, 3, 3)));
        TRANSFER_2.setStatus("Pending");
    }


    @Test
    public void getTransferById_returns_correct_transfer_information() {
        Transfer transfer1 = sut.getTransferById(3001);
        assertTransfersMatch(TRANSFER_1, transfer1);
        Transfer transfer2 = sut.getTransferById(3002);
        assertTransfersMatch(TRANSFER_2, transfer2);
    }

    @Test
    public void getTransferById_returns_null_when_account_id_not_found() {
        Transfer nullTransfer = sut.getTransferById(0);
        Assert.assertNull(nullTransfer);
    }

    @Test
    public void getTransfersByAccountId_returns_list_of_transfers_for_account_id() {
        List<Transfer> transfers1 = sut.getTransfersByAccountId(2001);
        Assert.assertEquals(2, transfers1.size());

        assertTransfersMatch(TRANSFER_1, transfers1.get(0));
        assertTransfersMatch(TRANSFER_2, transfers1.get(1));

        List<Transfer> transfers2 = sut.getTransfersByAccountId(2002);
        Assert.assertEquals(2, transfers2.size());

        assertTransfersMatch(TRANSFER_1, transfers2.get(0));
        assertTransfersMatch(TRANSFER_2, transfers2.get(1));
    }

    public void created_transfer_has_expected_values_when_retrieved() {
        Transfer testTransfer = new Transfer();
        testTransfer.setSenderAccountId(2001);
        testTransfer.setReceiverAccountId(2002);
        testTransfer.setTransferAmount(BigDecimal.valueOf(2000).setScale(2, RoundingMode.HALF_UP));
        testTransfer.setStatus("Approved");
        testTransfer.setTransferTimestamp(Timestamp.valueOf(LocalDateTime.of(2018, 3, 3, 3, 3, 3)));

        Transfer createdTransfer = sut.createTransfer(testTransfer);
        int newId = createdTransfer.getTransferId();
        Assert.assertTrue(newId > 3000);

        Transfer retrievedTransfer = sut.getTransferById(newId);
        assertTransfersMatch(createdTransfer, retrievedTransfer);

    }

    private void assertTransfersMatch(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getSenderAccountId(), actual.getSenderAccountId());
        Assert.assertEquals(expected.getReceiverAccountId(), actual.getReceiverAccountId());
        Assert.assertEquals(expected.getTransferAmount(), actual.getTransferAmount());
        Assert.assertEquals(expected.getTransferTimestamp(), actual.getTransferTimestamp());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
    }
}
