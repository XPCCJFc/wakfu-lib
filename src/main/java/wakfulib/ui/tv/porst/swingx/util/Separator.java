package wakfulib.ui.tv.porst.swingx.util;

public class Separator<T> {
    private T next;
    private T separator;

    /**
     * Constructs a separator with the specified initial value and remaining separator.
     *
     * @param initial
     *            the value to use for the first call
     * @param separator
     *            the value to use after the first call
     */
    public Separator(T initial, T separator) {
        this.next = initial;
        this.separator = separator;
    }

    /**
     * Returns the current value of the separator.
     *
     * @return the separator value
     */
    public T get() {
        T result = next;
        next = separator;

        return result;
    }
}
