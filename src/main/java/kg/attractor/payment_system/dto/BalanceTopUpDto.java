package kg.attractor.payment_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceTopUpDto {

    @NotBlank(message = "Номер счета обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер счета должен состоять из 16 цифр")
    private String accountNumber;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
    @Digits(integer = 17, fraction = 2, message = "Некорректный формат суммы")
    private BigDecimal amount;
}
