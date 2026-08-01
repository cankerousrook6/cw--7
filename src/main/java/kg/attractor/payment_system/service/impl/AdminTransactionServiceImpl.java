package kg.attractor.payment_system.service.impl;

import kg.attractor.payment_system.dao.AccountDao;
import kg.attractor.payment_system.dao.RollbackLogDao;
import kg.attractor.payment_system.dao.TransactionDao;
import kg.attractor.payment_system.dao.TransactionLogDao;
import kg.attractor.payment_system.dao.UserDao;
import kg.attractor.payment_system.dto.RollbackLogResponseDto;
import kg.attractor.payment_system.dto.TransactionLogResponseDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;
import kg.attractor.payment_system.exception.AccountNotFoundException;
import kg.attractor.payment_system.exception.InsufficientFundsException;
import kg.attractor.payment_system.exception.InvalidTransactionStateException;
import kg.attractor.payment_system.exception.RollbackLogNotFoundException;
import kg.attractor.payment_system.exception.RollbackNotAcceptableException;
import kg.attractor.payment_system.exception.TransactionNotFoundException;
import kg.attractor.payment_system.exception.UserNotFoundException;
import kg.attractor.payment_system.model.Account;
import kg.attractor.payment_system.model.PaymentTransaction;
import kg.attractor.payment_system.model.RollbackLog;
import kg.attractor.payment_system.model.TransactionLog;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import kg.attractor.payment_system.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final TransactionDao transactionDao;
    private final TransactionLogDao transactionLogDao;
    private final RollbackLogDao rollbackLogDao;
    private final AccountDao accountDao;
    private final UserDao userDao;

    @Override
    public List<TransactionLogResponseDto> getTransactionLog() {
        return transactionLogDao.findAll()
                .stream()
                .map(this::toLogResponse)
                .toList();
    }

    @Override
    public List<TransactionResponseDto> getPendingTransactions() {
        return transactionDao.findPending()
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponseDto approve(Long transactionId) {
        PaymentTransaction transaction = getTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Одобрить можно только ожидающую транзакцию");
        }

        Account sender = getAccount(transaction.getSenderAccountId());
        Account receiver = getAccount(transaction.getReceiverAccountId());

        if (!accountDao.subtractBalanceIfEnough(sender.getId(), transaction.getAmount())) {
            throw new InsufficientFundsException("На счете отправителя недостаточно средств для одобрения");
        }
        accountDao.addBalance(receiver.getId(), transaction.getAmount());

        LocalDateTime completedAt = LocalDateTime.now();
        transactionDao.markCompleted(transactionId, completedAt);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(completedAt);

        createTransactionLog(transaction, sender, receiver);
        log.info("Транзакция {} одобрена", transactionId);

        return toTransactionResponse(transaction, sender, receiver);
    }

    @Override
    @Transactional
    public RollbackLogResponseDto rollback(Long transactionId) {
        PaymentTransaction transaction = getTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidTransactionStateException("Откатить можно только завершенную транзакцию");
        }

        Account sender = getAccount(transaction.getSenderAccountId());
        Account receiver = getAccount(transaction.getReceiverAccountId());

        if (!accountDao.subtractBalanceIfEnough(receiver.getId(), transaction.getAmount())) {
            throw new RollbackNotAcceptableException(
                    "На счете получателя недостаточно средств для отката транзакции"
            );
        }
        accountDao.addBalance(sender.getId(), transaction.getAmount());

        LocalDateTime rolledBackAt = LocalDateTime.now();
        transactionDao.markRolledBack(transactionId, rolledBackAt);
        transactionLogDao.updateStatus(transactionId, TransactionStatus.ROLLED_BACK);

        RollbackLog rollbackLog = RollbackLog.builder()
                .transactionId(transactionId)
                .senderAccountNumber(sender.getAccountNumber())
                .receiverAccountNumber(receiver.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .createdAt(rolledBackAt)
                .build();

        Long rollbackLogId = rollbackLogDao.create(rollbackLog);
        rollbackLog.setId(rollbackLogId);

        log.info("Транзакция {} успешно отменена, запись отката {}", transactionId, rollbackLogId);
        return toRollbackResponse(rollbackLog);
    }

    @Override
    @Transactional
    public TransactionResponseDto markDeletedByRollbackLogId(Long rollbackLogId) {
        RollbackLog rollbackLog = rollbackLogDao.findById(rollbackLogId)
                .orElseThrow(() -> new RollbackLogNotFoundException("Запись журнала откатов не найдена"));

        PaymentTransaction transaction = getTransaction(rollbackLog.getTransactionId());

        if (transaction.getStatus() != TransactionStatus.ROLLED_BACK) {
            throw new InvalidTransactionStateException(
                    "Пометить транзакцию удаленной можно только после успешного отката"
            );
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        transactionDao.markDeleted(transaction.getId(), deletedAt);
        transactionLogDao.updateStatus(transaction.getId(), TransactionStatus.DELETED);

        transaction.setStatus(TransactionStatus.DELETED);
        transaction.setDeletedAt(deletedAt);

        log.info("Транзакция {} помечена как удаленная", transaction.getId());
        return toTransactionResponse(transaction);
    }

    private void createTransactionLog(
            PaymentTransaction transaction,
            Account senderAccount,
            Account receiverAccount
    ) {
        User sender = userDao.findById(senderAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Отправитель не найден"));
        User receiver = userDao.findById(receiverAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Получатель не найден"));

        TransactionLog log = TransactionLog.builder()
                .transactionId(transaction.getId())
                .senderUsername(sender.getUsername())
                .receiverUsername(receiver.getUsername())
                .senderAccountNumber(senderAccount.getAccountNumber())
                .receiverAccountNumber(receiverAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        transactionLogDao.create(log);
    }

    private PaymentTransaction getTransaction(Long transactionId) {
        return transactionDao.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));
    }

    private Account getAccount(Long accountId) {
        return accountDao.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден"));
    }

    private TransactionResponseDto toTransactionResponse(PaymentTransaction transaction) {
        return toTransactionResponse(
                transaction,
                getAccount(transaction.getSenderAccountId()),
                getAccount(transaction.getReceiverAccountId())
        );
    }

    private TransactionResponseDto toTransactionResponse(
            PaymentTransaction transaction,
            Account sender,
            Account receiver
    ) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .fromAccountNumber(sender.getAccountNumber())
                .toAccountNumber(receiver.getAccountNumber())
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

    private TransactionLogResponseDto toLogResponse(TransactionLog log) {
        return TransactionLogResponseDto.builder()
                .id(log.getId())
                .transactionId(log.getTransactionId())
                .senderUsername(log.getSenderUsername())
                .receiverUsername(log.getReceiverUsername())
                .senderAccountNumber(log.getSenderAccountNumber())
                .receiverAccountNumber(log.getReceiverAccountNumber())
                .amount(log.getAmount())
                .currency(log.getCurrency())
                .status(log.getStatus())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private RollbackLogResponseDto toRollbackResponse(RollbackLog log) {
        return RollbackLogResponseDto.builder()
                .id(log.getId())
                .transactionId(log.getTransactionId())
                .senderAccountNumber(log.getSenderAccountNumber())
                .receiverAccountNumber(log.getReceiverAccountNumber())
                .amount(log.getAmount())
                .currency(log.getCurrency())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
