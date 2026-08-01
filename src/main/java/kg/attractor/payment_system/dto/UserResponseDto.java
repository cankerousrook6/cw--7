package kg.attractor.payment_system.dto;

import kg.attractor.payment_system.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String phone;
    private String username;
    private Role role;
}
