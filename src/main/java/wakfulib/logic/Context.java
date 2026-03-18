package wakfulib.logic;

/**
 * Enumeration representing the network context (Client or Server).
 * This is used to determine the behavior of certain components like packet registration
 * and message handling.
 */
public enum Context {
    /** The library is running in server mode. */
    Server,
    /** The library is running in client mode. */
    Client
}
