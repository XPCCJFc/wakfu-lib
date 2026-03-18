package wakfulib.exception;

/**
 * Thrown when an invalid token is encountered.
 */
public class InvalidTokenException extends Exception {
    public InvalidTokenException(byte error) {
        super("Invalid token ! Error code : " + error);
    }
}
