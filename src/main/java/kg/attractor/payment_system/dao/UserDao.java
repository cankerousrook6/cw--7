package kg.attractor.payment_system.dao;

import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .phone(rs.getString("phone"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .role(Role.valueOf(rs.getString("role")))
            .enabled(rs.getBoolean("enabled"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Long create(User user) {
        String sql = """
                INSERT INTO users(phone, username, password, role, enabled, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = user.getCreatedAt() == null
                ? LocalDateTime.now()
                : user.getCreatedAt();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement =
                    connection.prepareStatement(
                            sql,
                            new String[]{"id"}
                    );
            statement.setString(1, user.getPhone());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, Boolean.TRUE.equals(user.getEnabled()));
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbcTemplate.query(sql, rowMapper, username).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone);
        return count != null && count > 0;
    }
}
