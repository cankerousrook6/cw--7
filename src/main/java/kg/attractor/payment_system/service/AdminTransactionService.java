package kg.attractor.payment_system.service;

import kg.attractor.payment_system.dto.RollbackLogResponseDto;
import kg.attractor.payment_system.dto.TransactionLogResponseDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;

import java.util.List;

public interface AdminTransactionService {
    List<TransactionLogResponseDto> getTransactionLog();
    List<TransactionResponseDto> getPendingTransactions();
    TransactionResponseDto approve(Long transactionId);
    RollbackLogResponseDto rollback(Long transactionId);
    TransactionResponseDto markDeletedByRollbackLogId(Long rollbackLogId);
}
