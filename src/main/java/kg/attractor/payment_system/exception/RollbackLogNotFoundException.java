package kg.attractor.payment_system.exception;

public class RollbackLogNotFoundException extends RuntimeException {
    public RollbackLogNotFoundException(String message) {
        super(message);
    }
}
