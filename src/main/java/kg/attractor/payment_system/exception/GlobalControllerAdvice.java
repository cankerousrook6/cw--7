package kg.attractor.payment_system.exception;

import jakarta.validation.ConstraintViolationException;
import kg.attractor.payment_system.service.ErrorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ErrorService errorService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseBody> validationHandler(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                errorService.makeValidationResponse(e.getBindingResult())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseBody> constraintViolationHandler(ConstraintViolationException e) {
        return build(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseBody> unreadableBodyHandler(HttpMessageNotReadableException e) {
        return build(new IllegalArgumentException("Некорректное тело запроса"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            AccountNotFoundException.class,
            TransactionNotFoundException.class,
            RollbackLogNotFoundException.class
    })
    public ResponseEntity<ErrorResponseBody> notFoundHandler(RuntimeException e) {
        return build(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            AccountLimitExceededException.class,
            AccountCurrencyAlreadyExistsException.class,
            InvalidTransactionStateException.class
    })
    public ResponseEntity<ErrorResponseBody> conflictHandler(RuntimeException e) {
        return build(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            CurrencyMismatchException.class,
            InvalidTransactionException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponseBody> badRequestHandler(RuntimeException e) {
        return build(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponseBody> forbiddenHandler(ForbiddenOperationException e) {
        return build(e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponseBody> insufficientFundsHandler(InsufficientFundsException e) {
        return build(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(RollbackNotAcceptableException.class)
    public ResponseEntity<ErrorResponseBody> rollbackHandler(RollbackNotAcceptableException e) {
        return build(e, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseBody> unexpectedHandler(Exception e) {
        log.error("Непредвиденная ошибка", e);
        return build(new RuntimeException("Внутренняя ошибка сервера"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseBody> build(Exception e, HttpStatus status) {
        log.warn("Ошибка обработки запроса: {}", e.getMessage());
        return ResponseEntity.status(status).body(errorService.makeResponse(e, status));
    }
}
