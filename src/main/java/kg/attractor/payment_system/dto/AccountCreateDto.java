package kg.attractor.payment_system.dto;

import jakarta.validation.constraints.NotNull;
import kg.attractor.payment_system.model.enums.CurrencyType;
import lombok.Data;

@Data
public class AccountCreateDto {

    @NotNull(message = "Валюта обязательна")
    private CurrencyType currency;
}
