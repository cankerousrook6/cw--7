package kg.attractor.payment_system.service;

import kg.attractor.payment_system.exception.ErrorResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

public interface ErrorService {
    ErrorResponseBody makeResponse(Exception exception, HttpStatus status);
    ErrorResponseBody makeValidationResponse(BindingResult bindingResult);
}
