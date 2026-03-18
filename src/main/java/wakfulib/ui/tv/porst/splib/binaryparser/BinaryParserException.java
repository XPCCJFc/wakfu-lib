package wakfulib.ui.tv.porst.splib.binaryparser;

/**
 * Exception class used to signal exceptions during binary stream parsing.
 */
public final class BinaryParserException extends RuntimeException {

	/**
	 * Creates a new exception object.
	 * 
	 * @param message The exception message.
	 */
	public BinaryParserException(String message) {
		super(message);
	}
}