package kg.attractor.payment_system.model;

import kg.attractor.payment_system.model.enums.CurrencyType;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    private Long id;
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;
    private CurrencyType currency;
    private TransactionStatus status;
    private Boolean requiresApproval;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime rolledBackAt;
    private LocalDateTime deletedAt;
}
