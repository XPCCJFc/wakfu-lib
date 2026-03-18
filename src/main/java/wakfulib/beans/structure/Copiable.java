package wakfulib.beans.structure;

/**
 * Interface for objects that support creating a copy of themselves.
 */
public interface Copiable {

    /**
     * Creates and returns a copy of this object.
     *
     * @return a copy of this instance
     */
    Object copy();
}
