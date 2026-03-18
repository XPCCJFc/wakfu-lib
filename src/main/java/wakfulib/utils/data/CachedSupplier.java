package wakfulib.utils.data;

import java.util.function.Supplier;

/**
 * A {@link Supplier} implementation that caches the result of the first call 
 * and returns it for all subsequent calls.
 * This is useful for lazy initialization of objects that are expensive to create.
 *
 * @param <T> the type of object supplied
 */
public class CachedSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T obj;
    private boolean untouched;

    /**
     * Creates a new CachedSupplier wrapping the provided supplier.
     *
     * @param supplier the source supplier to wrap
     */
    public CachedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
        untouched = true;
    }

    @Override
    public T get() {
        if (untouched) {
            untouched = false;
            obj = supplier.get();
        }
        return obj;
    }
}
