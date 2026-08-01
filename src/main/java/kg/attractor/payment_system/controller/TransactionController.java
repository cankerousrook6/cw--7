package kg.attractor.payment_system.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.attractor.payment_system.config.SwaggerConfig;
import kg.attractor.payment_system.dto.TransactionCreateDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;
import kg.attractor.payment_system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(
            Authentication authentication,
            @Valid @RequestBody TransactionCreateDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(authentication.getName(), dto));
    }

    @GetMapping("/{accountId}/history")
    public ResponseEntity<List<TransactionResponseDto>> getHistory(
            Authentication authentication,
            @PathVariable @Positive Long accountId
    ) {
        return ResponseEntity.ok(
                transactionService.getHistory(authentication.getName(), accountId)
        );
    }
}
