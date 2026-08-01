package kg.attractor.payment_system.dao;

import kg.attractor.payment_system.model.TransactionLog;
import kg.attractor.payment_system.model.enums.CurrencyType;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionLogDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TransactionLog> rowMapper = (rs, rowNum) -> TransactionLog.builder()
            .id(rs.getLong("id"))
            .transactionId(rs.getLong("transaction_id"))
            .senderUsername(rs.getString("sender_username"))
            .receiverUsername(rs.getString("receiver_username"))
            .senderAccountNumber(rs.getString("sender_account_number"))
            .receiverAccountNumber(rs.getString("receiver_account_number"))
            .amount(rs.getBigDecimal("amount"))
            .currency(CurrencyType.valueOf(rs.getString("currency")))
            .status(TransactionStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Long create(TransactionLog log) {
        String sql = """
                INSERT INTO transaction_logs(
                    transaction_id,
                    sender_username,
                    receiver_username,
                    sender_account_number,
                    receiver_account_number,
                    amount,
                    currency,
                    status,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            statement.setLong(1, log.getTransactionId());
            statement.setString(2, log.getSenderUsername());
            statement.setString(3, log.getReceiverUsername());
            statement.setString(4, log.getSenderAccountNumber());
            statement.setString(5, log.getReceiverAccountNumber());
            statement.setBigDecimal(6, log.getAmount());
            statement.setString(7, log.getCurrency().name());
            statement.setString(8, log.getStatus().name());
            statement.setTimestamp(9, Timestamp.valueOf(log.getCreatedAt()));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<TransactionLog> findAll() {
        String sql = "SELECT * FROM transaction_logs ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<TransactionLog> findByTransactionId(Long transactionId) {
        String sql = "SELECT * FROM transaction_logs WHERE transaction_id = ?";
        return jdbcTemplate.query(sql, rowMapper, transactionId).stream().findFirst();
    }

    public void updateStatus(Long transactionId, TransactionStatus status) {
        String sql = "UPDATE transaction_logs SET status = ? WHERE transaction_id = ?";
        jdbcTemplate.update(sql, status.name(), transactionId);
    }
}
