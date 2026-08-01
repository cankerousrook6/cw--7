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
public class TransactionLog {
    private Long id;
    private Long transactionId;
    private String senderUsername;
    private String receiverUsername;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private CurrencyType currency;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
