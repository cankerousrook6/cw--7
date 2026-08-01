package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.CurrencyType;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDto {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private CurrencyType currency;
    private TransactionStatus status;
    private Boolean requiresApproval;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime rolledBackAt;
    private LocalDateTime deletedAt;
}
