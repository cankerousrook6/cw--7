package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.CurrencyType;
import kg.attractor.payment_system.model.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionLogResponseDto {
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
