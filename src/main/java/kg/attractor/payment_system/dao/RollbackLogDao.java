package kg.attractor.payment_system.dao;

import kg.attractor.payment_system.model.RollbackLog;
import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RollbackLogDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RollbackLog> rowMapper = (rs, rowNum) -> RollbackLog.builder()
            .id(rs.getLong("id"))
            .transactionId(rs.getLong("transaction_id"))
            .senderAccountNumber(rs.getString("sender_account_number"))
            .receiverAccountNumber(rs.getString("receiver_account_number"))
            .amount(rs.getBigDecimal("amount"))
            .currency(CurrencyType.valueOf(rs.getString("currency")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Long create(RollbackLog log) {
        String sql = """
                INSERT INTO rollback_logs(
                    transaction_id,
                    sender_account_number,
                    receiver_account_number,
                    amount,
                    currency,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            statement.setLong(1, log.getTransactionId());
            statement.setString(2, log.getSenderAccountNumber());
            statement.setString(3, log.getReceiverAccountNumber());
            statement.setBigDecimal(4, log.getAmount());
            statement.setString(5, log.getCurrency().name());
            statement.setTimestamp(6, Timestamp.valueOf(log.getCreatedAt()));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Optional<RollbackLog> findById(Long id) {
        String sql = "SELECT * FROM rollback_logs WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }
}
