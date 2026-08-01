package kg.attractor.payment_system.controller;

import jakarta.validation.Valid;
import kg.attractor.payment_system.dto.RegisterRequestDto;
import kg.attractor.payment_system.dto.UserResponseDto;
import kg.attractor.payment_system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody RegisterRequestDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrationService.register(dto));
    }
}
