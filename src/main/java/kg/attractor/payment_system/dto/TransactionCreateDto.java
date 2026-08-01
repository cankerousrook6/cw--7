package kg.attractor.payment_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionCreateDto {

    @NotBlank(message = "Счет отправителя обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Счет отправителя должен состоять из 16 цифр")
    private String fromAccountNumber;

    @NotBlank(message = "Счет получателя обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Счет получателя должен состоять из 16 цифр")
    private String toAccountNumber;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
    @Digits(integer = 17, fraction = 2, message = "Некорректный формат суммы")
    private BigDecimal amount;
}
