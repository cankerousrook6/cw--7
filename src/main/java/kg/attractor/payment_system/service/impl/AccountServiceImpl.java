package kg.attractor.payment_system.service.impl;

import kg.attractor.payment_system.dao.AccountDao;
import kg.attractor.payment_system.dao.UserDao;
import kg.attractor.payment_system.dto.AccountCreateDto;
import kg.attractor.payment_system.dto.AccountResponseDto;
import kg.attractor.payment_system.dto.BalanceResponseDto;
import kg.attractor.payment_system.dto.BalanceTopUpDto;
import kg.attractor.payment_system.exception.AccountCurrencyAlreadyExistsException;
import kg.attractor.payment_system.exception.AccountLimitExceededException;
import kg.attractor.payment_system.exception.AccountNotFoundException;
import kg.attractor.payment_system.exception.ForbiddenOperationException;
import kg.attractor.payment_system.exception.UserNotFoundException;
import kg.attractor.payment_system.model.Account;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.Role;
import kg.attractor.payment_system.service.AccountService;
import kg.attractor.payment_system.util.AccountNumberGenerator;
import kg.attractor.payment_system.dao.TransactionLogDao;
import kg.attractor.payment_system.model.TransactionLog;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int MAX_ACCOUNTS_PER_USER = 3;
    private static final int MAX_NUMBER_GENERATION_ATTEMPTS = 20;

    private final AccountDao accountDao;
    private final UserDao userDao;
    private final TransactionLogDao transactionLogDao;
    private final AccountNumberGenerator accountNumberGenerator;


    @Override
    @Transactional
    public AccountResponseDto create(String username, AccountCreateDto dto) {
        User user = getUser(username);

        if (accountDao.countByUserId(user.getId()) >= MAX_ACCOUNTS_PER_USER) {
            throw new AccountLimitExceededException("У пользователя не может быть больше трех счетов");
        }

        if (accountDao.existsByUserIdAndCurrency(user.getId(), dto.getCurrency())) {
            throw new AccountCurrencyAlreadyExistsException(
                    "У пользователя уже есть счет в валюте " + dto.getCurrency()
            );
        }

        Account account = Account.builder()
                .userId(user.getId())
                .accountNumber(generateUniqueAccountNumber())
                .currency(dto.getCurrency())
                .balance(BigDecimal.ZERO.setScale(2))
                .createdAt(LocalDateTime.now())
                .build();

        Long id = accountDao.create(account);
        account.setId(id);

        log.info("Создан счет {} для пользователя {}", account.getAccountNumber(), username);
        return toResponse(account);
    }

    @Override
    public BalanceResponseDto getBalance(String username, String accountNumber) {
        User user = getUser(username);
        Account account = getAccount(accountNumber);

        if (!account.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Нельзя просматривать баланс чужого счета");
        }

        return toBalanceResponse(account);
    }

    @Override
    @Transactional
    public BalanceResponseDto topUp(BalanceTopUpDto dto) {
        Account account = getAccount(dto.getAccountNumber());

        User owner = userDao.findById(account.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Владелец счета не найден"
                        )
                );

        LocalDateTime createdAt = LocalDateTime.now();

        accountDao.addBalance(
                account.getId(),
                dto.getAmount()
        );

        TransactionLog transactionLog = TransactionLog.builder()
                .transactionId(null)
                .senderUsername("SYSTEM")
                .receiverUsername(owner.getUsername())
                .senderAccountNumber("0000000000000000")
                .receiverAccountNumber(account.getAccountNumber())
                .amount(dto.getAmount())
                .currency(account.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .createdAt(createdAt)
                .build();

        transactionLogDao.create(transactionLog);

        log.info(
                "Счет {} пополнен на сумму {} {}",
                account.getAccountNumber(),
                dto.getAmount(),
                account.getCurrency()
        );

        Account updatedAccount =
                getAccount(dto.getAccountNumber());

        return toBalanceResponse(updatedAccount);
    }

    @Override
    public List<AccountResponseDto> findCurrentUserAccounts(String username) {
        User user = getUser(username);
        return accountDao.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getUser(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private Account getAccount(String accountNumber) {
        return accountDao.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден"));
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_NUMBER_GENERATION_ATTEMPTS; attempt++) {
            String accountNumber = accountNumberGenerator.generate();
            if (!accountDao.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new IllegalStateException("Не удалось сгенерировать уникальный номер счета");
    }

    private AccountResponseDto toResponse(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private BalanceResponseDto toBalanceResponse(Account account) {
        return BalanceResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }
}
