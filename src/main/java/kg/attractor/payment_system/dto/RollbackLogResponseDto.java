package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RollbackLogResponseDto {
    private Long id;
    private Long transactionId;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private CurrencyType currency;
    private LocalDateTime createdAt;
}
