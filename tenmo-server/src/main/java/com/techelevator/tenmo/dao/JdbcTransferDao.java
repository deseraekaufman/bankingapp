package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {


    private JdbcTemplate jdbcTemplate;
    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}


    @Override
    public void create(Transfer transfer) {
        idExists(transfer);
        String sql = "INSERT INTO transfer (account_from, account_to, transfer_status_id, amount, transfer_type_id) \n" +
                "VALUES ((SELECT account_id FROM account WHERE user_id = ?), (SELECT account_id FROM account WHERE user_id = ?), 2, ?, 2);";
        jdbcTemplate.update(sql, transfer.getIdFrom(), transfer.getIdTo(), transfer.getAmount());

        adjustBalance(transfer);

    }

    public void adjustBalance(Transfer transfer) {
        String sql;
        sql = "UPDATE account \n" +
                "SET balance = balance + ?\n" +
                "WHERE user_id=?;";
        jdbcTemplate.update(sql, transfer.getAmount(), transfer.getIdTo());
        sql = "UPDATE account \n" +
                "SET balance = balance - ?\n" +
                "WHERE user_id=?;";
        jdbcTemplate.update(sql, transfer.getAmount(), transfer.getIdFrom());
    }

    public Transfer getTransferByTransferId(int id){
        String sql = "SELECT * FROM transfer WHERE transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if(results.next()){
           return mapRowToTransfer(results);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void idExists (Transfer transfer) {
        String sql = "SELECT * FROM account WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transfer.getIdFrom());
        if (!results.next()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        SqlRowSet idToResults = jdbcTemplate.queryForRowSet(sql, transfer.getIdTo());
        if (!idToResults.next()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public List<TransferDTO> getUserTransfer(int userId){

        List<TransferDTO> list = new ArrayList<>();
        String sql = "SELECT transfer_id, amount,\n" +
                "(SELECT username WHERE account_id = account_from ) AS user_from,\n" +
                "(SELECT username WHERE account_id = account_to ) AS user_to\n" +
                "FROM transfer\n" +
                "JOIN account ON account.account_id = transfer.account_to OR account.account_id = transfer.account_from\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE (account_from = (SELECT account_id FROM account WHERE user_id = ?) OR\n" +
                "account_to = (SELECT account_id FROM account WHERE user_id = ?))\n" +
                "AND tenmo_user.user_id != ? AND transfer_status_id = 2;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId, userId);
        while(results.next()){
            list.add(mapRowToLimitedTransfer(results));
        }
        return list;
    }

    public TransferDTO getTransferById(int transferId, int userId){
        TransferDTO tf = new TransferDTO();
        String sql = "SELECT transfer_id, (SELECT username FROM tenmo_user\n" +
                "\t\t\t\t\t JOIN account ON tenmo_user.user_id = account.user_id \n" +
                "\t\t\t\t\t WHERE account_id = account_from ) AS user_from, \n" +
                "\t\t\t\t\t (SELECT username FROM tenmo_user \n" +
                "\t\t\t\t\t  JOIN account ON tenmo_user.user_id = account.user_id\n" +
                "\t\t\t\t\t  WHERE account_id = account_to ) AS user_to, transfer_status_desc AS transfer_status, transfer_type_desc AS transfer_type, amount AS transfer_amount\n" +
                "FROM tenmo_user\n" +
                "JOIN account ON tenmo_user.user_id = account.user_id\n" +
                "JOIN transfer ON account_id = account_from OR account_id = account_to\n" +
                "JOIN transfer_type ON transfer.transfer_type_id = transfer_type.transfer_type_id\n" +
                "JOIN transfer_status ON transfer.transfer_status_id = transfer_status.transfer_status_id\n" +
                "WHERE (account_from = (SELECT account_id FROM account WHERE user_id = ?)\n" +
                "\t\tOR account_to = (SELECT account_id FROM account WHERE user_id = ?))\n" +
                "               AND tenmo_user.user_id != ? AND transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId, userId, transferId);
        if(results.next()){
            tf = mapRowToTransferDTO(results);
        }
        return tf;
    }

    public void pending(Transfer transfer) {
        idExists(transfer);
        String sql = "INSERT INTO transfer (account_from, account_to, transfer_status_id, amount, transfer_type_id) \n" +
                "VALUES ((SELECT account_id FROM account WHERE user_id = ?), (SELECT account_id FROM account WHERE user_id = ?), 1, ?, 1);";
        jdbcTemplate.update(sql, transfer.getIdFrom(), transfer.getIdTo(), transfer.getAmount());

    }

    public void updatePending(Transfer transfer) {

        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";

        jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());

    }

    public List<TransferDTO> getPendingTransfer(int userId) {

        List<TransferDTO> list = new ArrayList<>();
        String sql = "SELECT transfer_id, amount,\n" +
                "(SELECT username WHERE account_id = account_from ) AS user_from,\n" +
                "(SELECT username WHERE account_id = account_to ) AS user_to\n" +
                "FROM transfer\n" +
                "JOIN account ON account.account_id = transfer.account_to OR account.account_id = transfer.account_from\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE (account_from = (SELECT account_id FROM account WHERE user_id = ?) OR\n" +
                "account_to = (SELECT account_id FROM account WHERE user_id = ?))\n" +
                "AND tenmo_user.user_id != ? AND transfer_status_id = 1;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId, userId);
        while (results.next()) {
            list.add(mapRowToLimitedTransfer(results));
        }
        return list;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs){
        Transfer tf = new Transfer();
        tf.setTransferStatusId(rs.getInt("transfer_status_id"));
        tf.setIdFrom(rs.getInt("account_from"));
        tf.setIdTo(rs.getInt("account_to"));
        tf.setTransferId(rs.getInt("transfer_id"));
        tf.setAmount(rs.getDouble("amount"));
        tf.setTransferTypeId(rs.getInt("transfer_type_id"));
        return tf;
    }


    private TransferDTO mapRowToTransferDTO(SqlRowSet rs) {
        TransferDTO tf = new TransferDTO();
        tf.setTransferID(rs.getInt("transfer_id"));
        tf.setUserFrom(rs.getString("user_from"));
        tf.setUserTo(rs.getString("user_to"));
        tf.setTransferStatus(rs.getString("transfer_status"));
        tf.setTransferType(rs.getString("transfer_type"));
        tf.setTransferAmount(rs.getDouble("transfer_amount"));
        return tf;
    }

    private TransferDTO mapRowToLimitedTransfer(SqlRowSet rs) {
        TransferDTO tf = new TransferDTO();
        tf.setTransferID(rs.getInt("transfer_id"));
        tf.setUserTo(rs.getString("user_to"));
        tf.setTransferAmount(rs.getDouble("amount"));
        tf.setUserFrom(rs.getString("user_from"));
        return tf;
    }

}
