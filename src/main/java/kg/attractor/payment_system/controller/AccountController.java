package kg.attractor.payment_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import kg.attractor.payment_system.config.SwaggerConfig;
import kg.attractor.payment_system.dto.AccountCreateDto;
import kg.attractor.payment_system.dto.AccountResponseDto;
import kg.attractor.payment_system.dto.BalanceResponseDto;
import kg.attractor.payment_system.dto.BalanceTopUpDto;
import kg.attractor.payment_system.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<AccountResponseDto> create(
            Authentication authentication,
            @Valid @RequestBody AccountCreateDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.create(authentication.getName(), dto));
    }

    @GetMapping("/balance")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<BalanceResponseDto> getBalance(
            Authentication authentication,
            @RequestParam
            @Pattern(regexp = "^\\d{16}$", message = "Номер счета должен состоять из 16 цифр")
            String accountNumber
    ) {
        return ResponseEntity.ok(
                accountService.getBalance(authentication.getName(), accountNumber)
        );
    }

    @PostMapping("/balance")
    @Operation(description = "Публичное пополнение счета без авторизации")
    public ResponseEntity<BalanceResponseDto> topUp(
            @Valid @RequestBody BalanceTopUpDto dto
    ) {
        return ResponseEntity.ok(accountService.topUp(dto));
    }

    @GetMapping
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<List<AccountResponseDto>> getAccounts(Authentication authentication) {
        return ResponseEntity.ok(
                accountService.findCurrentUserAccounts(authentication.getName())
        );
    }
}
