package kg.attractor.payment_system.service;

import kg.attractor.payment_system.dto.AccountCreateDto;
import kg.attractor.payment_system.dto.AccountResponseDto;
import kg.attractor.payment_system.dto.BalanceResponseDto;
import kg.attractor.payment_system.dto.BalanceTopUpDto;

import java.util.List;

public interface AccountService {
    AccountResponseDto create(String username, AccountCreateDto dto);
    BalanceResponseDto getBalance(String username, String accountNumber);
    BalanceResponseDto topUp(BalanceTopUpDto dto);
    List<AccountResponseDto> findCurrentUserAccounts(String username);
}