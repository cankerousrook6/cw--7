package kg.attractor.payment_system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransactionIdDto {

    @NotNull(message = "ID транзакции обязателен")
    @Positive(message = "ID транзакции должен быть положительным")
    private Long transactionId;
}
