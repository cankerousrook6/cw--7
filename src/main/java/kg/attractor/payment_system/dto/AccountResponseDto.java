package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private CurrencyType currency;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
