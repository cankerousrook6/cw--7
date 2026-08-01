package kg.attractor.payment_system.service.impl;

import kg.attractor.payment_system.exception.ErrorResponseBody;
import kg.attractor.payment_system.service.ErrorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ErrorServiceImpl implements ErrorService {

    @Override
    public ErrorResponseBody makeResponse(Exception exception, HttpStatus status) {
        return ErrorResponseBody.builder()
                .title(exception.getMessage())
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .reasons(Map.of())
                .build();
    }

    @Override
    public ErrorResponseBody makeValidationResponse(BindingResult bindingResult) {
        Map<String, List<String>> reasons = new LinkedHashMap<>();

        for (FieldError error : bindingResult.getFieldErrors()) {
            reasons.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }

        return ErrorResponseBody.builder()
                .title("Validation error")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .reasons(reasons)
                .build();
    }
}
