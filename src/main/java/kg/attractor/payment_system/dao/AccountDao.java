package kg.attractor.payment_system.dao;

import kg.attractor.payment_system.model.Account;
import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Account> rowMapper = (rs, rowNum) -> Account.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .accountNumber(rs.getString("account_number"))
            .currency(CurrencyType.valueOf(rs.getString("currency")))
            .balance(rs.getBigDecimal("balance"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Long create(Account account) {
        String sql = """
                INSERT INTO accounts(user_id, account_number, currency, balance, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = account.getCreatedAt() == null
                ? LocalDateTime.now()
                : account.getCreatedAt();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            statement.setLong(1, account.getUserId());
            statement.setString(2, account.getAccountNumber());
            statement.setString(3, account.getCurrency().name());
            statement.setBigDecimal(4, account.getBalance());
            statement.setTimestamp(5, Timestamp.valueOf(createdAt));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        return jdbcTemplate.query(sql, rowMapper, accountNumber).stream().findFirst();
    }

    public List<Account> findByUserId(Long userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count == null ? 0 : count;
    }

    public boolean existsByUserIdAndCurrency(Long userId, CurrencyType currency) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE user_id = ? AND currency = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, currency.name());
        return count != null && count > 0;
    }

    public boolean existsByAccountNumber(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountNumber);
        return count != null && count > 0;
    }

    public void addBalance(Long accountId, BigDecimal amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        jdbcTemplate.update(sql, amount, accountId);
    }

    public boolean subtractBalanceIfEnough(Long accountId, BigDecimal amount) {
        String sql = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE id = ? AND balance >= ?
                """;
        return jdbcTemplate.update(sql, amount, accountId, amount) == 1;
    }
}
