package kg.attractor.payment_system.dao;

import kg.attractor.payment_system.model.PaymentTransaction;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PaymentTransaction> rowMapper = (rs, rowNum) -> PaymentTransaction.builder()
            .id(rs.getLong("id"))
            .senderAccountId(rs.getLong("sender_account_id"))
            .receiverAccountId(rs.getLong("receiver_account_id"))
            .amount(rs.getBigDecimal("amount"))
            .currency(CurrencyType.valueOf(rs.getString("currency")))
            .status(TransactionStatus.valueOf(rs.getString("status")))
            .requiresApproval(rs.getBoolean("requires_approval"))
            .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
            .completedAt(toLocalDateTime(rs.getTimestamp("completed_at")))
            .rolledBackAt(toLocalDateTime(rs.getTimestamp("rolled_back_at")))
            .deletedAt(toLocalDateTime(rs.getTimestamp("deleted_at")))
            .build();

    public Long create(PaymentTransaction transaction) {
        String sql = """
                INSERT INTO transactions(
                    sender_account_id,
                    receiver_account_id,
                    amount,
                    currency,
                    status,
                    requires_approval,
                    created_at,
                    completed_at,
                    rolled_back_at,
                    deleted_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            statement.setLong(1, transaction.getSenderAccountId());
            statement.setLong(2, transaction.getReceiverAccountId());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setString(4, transaction.getCurrency().name());
            statement.setString(5, transaction.getStatus().name());
            statement.setBoolean(6, Boolean.TRUE.equals(transaction.getRequiresApproval()));
            statement.setTimestamp(7, Timestamp.valueOf(transaction.getCreatedAt()));
            setNullableTimestamp(statement, 8, transaction.getCompletedAt());
            setNullableTimestamp(statement, 9, transaction.getRolledBackAt());
            setNullableTimestamp(statement, 10, transaction.getDeletedAt());
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Optional<PaymentTransaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public List<PaymentTransaction> findPending() {
        String sql = "SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY created_at";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<PaymentTransaction> findHistoryByAccountId(Long accountId) {
        String sql = """
                SELECT * FROM transactions
                WHERE sender_account_id = ? OR receiver_account_id = ?
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, rowMapper, accountId, accountId);
    }

    public void markCompleted(Long transactionId, LocalDateTime completedAt) {
        String sql = "UPDATE transactions SET status = 'COMPLETED', completed_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(completedAt), transactionId);
    }

    public void markRolledBack(Long transactionId, LocalDateTime rolledBackAt) {
        String sql = "UPDATE transactions SET status = 'ROLLED_BACK', rolled_back_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(rolledBackAt), transactionId);
    }

    public void markDeleted(Long transactionId, LocalDateTime deletedAt) {
        String sql = "UPDATE transactions SET status = 'DELETED', deleted_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(deletedAt), transactionId);
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static void setNullableTimestamp(
            PreparedStatement statement,
            int index,
            LocalDateTime value
    ) throws java.sql.SQLException {
        if (value == null) {
            statement.setTimestamp(index, null);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }
}
