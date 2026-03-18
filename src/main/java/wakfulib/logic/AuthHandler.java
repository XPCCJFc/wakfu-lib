package wakfulib.logic;

/**
 * Interface for handling authentication logic and retrieving results.
 * Implementations should process authentication messages and provide the necessary
 * information to connect to the selected world server.
 */
public interface AuthHandler {

    /**
     * Gets the address of the selected world server.
     *
     * @return The world server address.
     */
    String getSelectedAddress();

    /**
     * Gets the port of the selected world server.
     *
     * @return The world server port.
     */
    int getSelectedPort();

    /**
     * Gets the authentication token obtained during the login process.
     *
     * @return The authentication token.
     */
    String getToken();

    /**
     * Gets the result code of the authentication attempt.
     *
     * @return The result code (e.g., 0 for success, or an error code).
     */
    int getResultCode();
}
