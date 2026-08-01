package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceResponseDto {
    private String accountNumber;
    private CurrencyType currency;
    private BigDecimal balance;
}
