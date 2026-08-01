package kg.attractor.payment_system.service;

import kg.attractor.payment_system.dto.RegisterRequestDto;
import kg.attractor.payment_system.dto.UserResponseDto;

public interface RegistrationService {
    UserResponseDto register(RegisterRequestDto dto);
}
