package kg.attractor.payment_system.service;

import kg.attractor.payment_system.dto.TransactionCreateDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;

import java.util.List;

public interface TransactionService {
    TransactionResponseDto create(String username, TransactionCreateDto dto);
    List<TransactionResponseDto> getHistory(String username, Long accountId);
}
