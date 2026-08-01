package kg.attractor.payment_system.exception;

public class RollbackNotAcceptableException extends RuntimeException {
    public RollbackNotAcceptableException(String message) {
        super(message);
    }
}
