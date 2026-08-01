package kg.attractor.payment_system.model;

import kg.attractor.payment_system.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String phone;
    private String username;
    private String password;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
