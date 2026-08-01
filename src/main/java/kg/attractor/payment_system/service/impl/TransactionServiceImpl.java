package kg.attractor.payment_system.service.impl;

import kg.attractor.payment_system.dao.AccountDao;
import kg.attractor.payment_system.dao.TransactionDao;
import kg.attractor.payment_system.dao.TransactionLogDao;
import kg.attractor.payment_system.dao.UserDao;
import kg.attractor.payment_system.dto.TransactionCreateDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;
import kg.attractor.payment_system.exception.AccountNotFoundException;
import kg.attractor.payment_system.exception.CurrencyMismatchException;
import kg.attractor.payment_system.exception.ForbiddenOperationException;
import kg.attractor.payment_system.exception.InsufficientFundsException;
import kg.attractor.payment_system.exception.InvalidTransactionException;
import kg.attractor.payment_system.exception.UserNotFoundException;
import kg.attractor.payment_system.model.Account;
import kg.attractor.payment_system.model.PaymentTransaction;
import kg.attractor.payment_system.model.TransactionLog;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.Role;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import kg.attractor.payment_system.service.TransactionService;
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
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal APPROVAL_LIMIT = new BigDecimal("10.00");

    private final TransactionDao transactionDao;
    private final TransactionLogDao transactionLogDao;
    private final AccountDao accountDao;
    private final UserDao userDao;

    @Override
    @Transactional
    public TransactionResponseDto create(String username, TransactionCreateDto dto) {
        User sender = getUser(username);
        Account fromAccount = getAccountByNumber(dto.getFromAccountNumber());
        Account toAccount = getAccountByNumber(dto.getToAccountNumber());

        if (!fromAccount.getUserId().equals(sender.getId())) {
            throw new ForbiddenOperationException("Списание разрешено только со своего счета");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidTransactionException("Нельзя переводить средства на тот же счет");
        }

        if (fromAccount.getCurrency() != toAccount.getCurrency()) {
            throw new CurrencyMismatchException("Перевод возможен только между счетами в одной валюте");
        }

        if (fromAccount.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на счете отправителя");
        }

        boolean requiresApproval = dto.getAmount().compareTo(APPROVAL_LIMIT) > 0;
        LocalDateTime now = LocalDateTime.now();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .senderAccountId(fromAccount.getId())
                .receiverAccountId(toAccount.getId())
                .amount(dto.getAmount())
                .currency(fromAccount.getCurrency())
                .status(requiresApproval ? TransactionStatus.PENDING : TransactionStatus.COMPLETED)
                .requiresApproval(requiresApproval)
                .createdAt(now)
                .completedAt(requiresApproval ? null : now)
                .build();

        if (!requiresApproval) {
            executeTransfer(fromAccount, toAccount, dto.getAmount());
        }

        Long transactionId = transactionDao.create(transaction);
        transaction.setId(transactionId);

        if (!requiresApproval) {
            createTransactionLog(transaction, fromAccount, toAccount);
        }

        log.info("Создана транзакция {}, статус {}", transactionId, transaction.getStatus());
        return toResponse(transaction, fromAccount, toAccount);
    }

    @Override
    public List<TransactionResponseDto> getHistory(String username, Long accountId) {
        User user = getUser(username);
        Account account = getAccountById(accountId);

        if (!account.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Нельзя просматривать историю чужого счета");
        }

        return transactionDao.findHistoryByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void executeTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (!accountDao.subtractBalanceIfEnough(fromAccount.getId(), amount)) {
            throw new InsufficientFundsException("Недостаточно средств на счете отправителя");
        }
        accountDao.addBalance(toAccount.getId(), amount);
    }

    private void createTransactionLog(
            PaymentTransaction transaction,
            Account fromAccount,
            Account toAccount
    ) {
        User sender = userDao.findById(fromAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Отправитель не найден"));
        User receiver = userDao.findById(toAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Получатель не найден"));

        TransactionLog log = TransactionLog.builder()
                .transactionId(transaction.getId())
                .senderUsername(sender.getUsername())
                .receiverUsername(receiver.getUsername())
                .senderAccountNumber(fromAccount.getAccountNumber())
                .receiverAccountNumber(toAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .createdAt(LocalDateTime.now())
                .build();

        transactionLogDao.create(log);
    }

    private TransactionResponseDto toResponse(PaymentTransaction transaction) {
        Account fromAccount = getAccountById(transaction.getSenderAccountId());
        Account toAccount = getAccountById(transaction.getReceiverAccountId());
        return toResponse(transaction, fromAccount, toAccount);
    }

    private TransactionResponseDto toResponse(
            PaymentTransaction transaction,
            Account fromAccount,
            Account toAccount
    ) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .requiresApproval(transaction.getRequiresApproval())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .rolledBackAt(transaction.getRolledBackAt())
                .deletedAt(transaction.getDeletedAt())
                .build();
    }

    private User getUser(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private Account getAccountByNumber(String accountNumber) {
        return accountDao.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден: " + accountNumber));
    }

    private Account getAccountById(Long accountId) {
        return accountDao.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден"));
    }
}
