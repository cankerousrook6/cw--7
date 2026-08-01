package kg.attractor.payment_system.bootstrap;

import kg.attractor.payment_system.dao.AccountDao;
import kg.attractor.payment_system.dao.UserDao;
import kg.attractor.payment_system.model.Account;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.CurrencyType;
import kg.attractor.payment_system.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final AccountDao accountDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfMissing(
                "(996) (000) 00-00-00",
                "12345",
                "qwerty",
                Role.ADMIN
        );

        Long user1Id = createUserIfMissing(
                "(996) (555) 11-11-11",
                "user1",
                "qwerty",
                Role.USER
        );

        Long user2Id = createUserIfMissing(
                "(996) (555) 22-22-22",
                "user2",
                "qwerty",
                Role.USER
        );

        createAccountIfMissing(user1Id, "7000000000000001", CurrencyType.KGS, new BigDecimal("1000.00"));
        createAccountIfMissing(user1Id, "7000000000000002", CurrencyType.USD, new BigDecimal("100.00"));
        createAccountIfMissing(user2Id, "7000000000000003", CurrencyType.KGS, new BigDecimal("500.00"));

        log.info("Начальные данные проверены и загружены");
    }

    private Long createUserIfMissing(
            String phone,
            String username,
            String rawPassword,
            Role role
    ) {
        return userDao.findByUsername(username)
                .map(User::getId)
                .orElseGet(() -> userDao.create(User.builder()
                        .phone(phone)
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build()));
    }

    private void createAccountIfMissing(
            Long userId,
            String accountNumber,
            CurrencyType currency,
            BigDecimal balance
    ) {
        if (!accountDao.existsByAccountNumber(accountNumber)) {
            accountDao.create(Account.builder()
                    .userId(userId)
                    .accountNumber(accountNumber)
                    .currency(currency)
                    .balance(balance)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
