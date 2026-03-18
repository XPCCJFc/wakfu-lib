package wakfulib.exception;

/**
 * Thrown when a requested character cannot be found.
 */
public class CharacterNotFoundException extends Exception {
    public CharacterNotFoundException(String characterName) {
        super("Character " + characterName + " not found");
    }
}
