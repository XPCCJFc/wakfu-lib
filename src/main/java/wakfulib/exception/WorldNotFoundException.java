package wakfulib.exception;

/**
 * Thrown when a requested world cannot be found.
 */
public class WorldNotFoundException extends Exception {
    public WorldNotFoundException(byte error) {
        super("World not found ! Error code : " + error);
    }
}
