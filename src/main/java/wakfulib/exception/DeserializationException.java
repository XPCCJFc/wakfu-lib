package wakfulib.exception;

/**
 * Thrown when an error occurs during the deserialization of data,
 * such as when encountering unexpected or unknown data formats.
 */
public class DeserializationException extends RuntimeException {
    public DeserializationException() {
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
