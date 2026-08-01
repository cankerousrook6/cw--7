package kg.attractor.payment_system.service.impl;

import kg.attractor.payment_system.dao.UserDao;
import kg.attractor.payment_system.dto.RegisterRequestDto;
import kg.attractor.payment_system.dto.UserResponseDto;
import kg.attractor.payment_system.exception.UserAlreadyExistsException;
import kg.attractor.payment_system.model.User;
import kg.attractor.payment_system.model.enums.Role;
import kg.attractor.payment_system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        log.info("Регистрация пользователя: {}", dto.getUsername());

        if (userDao.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (userDao.existsByPhone(dto.getPhone())) {
            throw new UserAlreadyExistsException("Пользователь с таким номером телефона уже существует");
        }

        User user = User.builder()
                .phone(dto.getPhone())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        Long id = userDao.create(user);
        user.setId(id);

        return UserResponseDto.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
