package kg.attractor.payment_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.attractor.payment_system.config.SwaggerConfig;
import kg.attractor.payment_system.dto.RollbackLogResponseDto;
import kg.attractor.payment_system.dto.TransactionIdDto;
import kg.attractor.payment_system.dto.TransactionLogResponseDto;
import kg.attractor.payment_system.dto.TransactionResponseDto;
import kg.attractor.payment_system.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @GetMapping
    public ResponseEntity<List<TransactionLogResponseDto>> getTransactionLog() {
        return ResponseEntity.ok(adminTransactionService.getTransactionLog());
    }

    @GetMapping("/approval")
    public ResponseEntity<List<TransactionResponseDto>> getPendingTransactions() {
        return ResponseEntity.ok(adminTransactionService.getPendingTransactions());
    }

    @PostMapping("/approval")
    public ResponseEntity<TransactionResponseDto> approve(
            @Valid @RequestBody TransactionIdDto dto
    ) {
        return ResponseEntity.ok(adminTransactionService.approve(dto.getTransactionId()));
    }

    @PostMapping("/rollback")
    public ResponseEntity<RollbackLogResponseDto> rollback(
            @Valid @RequestBody TransactionIdDto dto
    ) {
        return ResponseEntity.ok(adminTransactionService.rollback(dto.getTransactionId()));
    }

    @DeleteMapping("/{id}")
    @Operation(description = "ID в пути — идентификатор записи из журнала откатов. Физического удаления нет.")
    public ResponseEntity<TransactionResponseDto> markDeleted(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(adminTransactionService.markDeletedByRollbackLogId(id));
    }
}
