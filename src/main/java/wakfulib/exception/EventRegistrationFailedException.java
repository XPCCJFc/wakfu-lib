package wakfulib.exception;

/**
 * Thrown when an event fails to be registered.
 */
public class EventRegistrationFailedException extends RuntimeException {

    public EventRegistrationFailedException(String message) {
        super(message);
    }

    public EventRegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
