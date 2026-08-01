package kg.attractor.payment_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(
            regexp = "^\\(996\\) \\(\\d{3}\\) \\d{2}-\\d{2}-\\d{2}$",
            message = "Формат телефона: (996) (XXX) XX-XX-XX"
    )
    private String phone;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    private String password;
}
