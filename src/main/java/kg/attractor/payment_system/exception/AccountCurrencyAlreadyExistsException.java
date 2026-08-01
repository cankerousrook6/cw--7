package kg.attractor.payment_system.exception;

public class AccountCurrencyAlreadyExistsException extends RuntimeException {
    public AccountCurrencyAlreadyExistsException(String message) {
        super(message);
    }
}
