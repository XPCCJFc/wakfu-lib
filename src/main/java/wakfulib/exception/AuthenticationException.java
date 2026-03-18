package wakfulib.exception;

/**
 * Thrown when an authentication attempt fails.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String error) {
        super("Authentication failed ! Error : " + error);
    }
}
