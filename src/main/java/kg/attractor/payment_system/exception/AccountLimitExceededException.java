package kg.attractor.payment_system.exception;

public class AccountLimitExceededException extends RuntimeException {
    public AccountLimitExceededException(String message) {
        super(message);
    }
}
