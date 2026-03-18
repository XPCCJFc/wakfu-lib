package wakfulib.exception;

/**
 * Thrown when a cryptographic operation fails.
 */
public class CryptoException extends Exception {

    public CryptoException(Throwable cause) {
        super(cause);
    }
}
