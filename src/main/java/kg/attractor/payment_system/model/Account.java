package kg.attractor.payment_system.model;

import kg.attractor.payment_system.model.enums.CurrencyType;
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
public class Account {
    private Long id;
    private Long userId;
    private String accountNumber;
    private CurrencyType currency;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
