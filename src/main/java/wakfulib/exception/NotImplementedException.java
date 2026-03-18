package wakfulib.exception;

import lombok.Getter;
import wakfulib.annotation.NonNull;
import wakfulib.annotation.Nullable;

/**
 * Thrown to indicate that a requested operation or feature has not been implemented.
 */
public class NotImplementedException extends RuntimeException {

    /**
     * The result associated with the unimplemented operation, if any.
     */
    @Getter
    public final Object res;
    
    /**
     * Creates an exception for an operation that is not implemented, without the default suffix.
     *
     * @param s the operation name
     * @return a new NotImplementedException
     */
    public static NotImplementedException noSuffix(@NonNull String s) {
        return new NotImplementedException(s, false);
    }

    public NotImplementedException() {
        this(null, "Not implemented !");
    }

    public NotImplementedException(@NonNull String operation) {
        this(operation, true);
    }

    public NotImplementedException(@NonNull String operation, boolean suffix) {
        super(operation + (suffix ? " is not implemented !" : ""));
        this.res = null;
    }

    public NotImplementedException(@Nullable Object res) {
        this(res, "Not implemented !");
    }

    /**
     * Creates an exception for an operation that is not implemented, with a result and without the default suffix.
     *
     * @param s the operation name
     * @param res the associated result
     * @return a new NotImplementedException
     */
    public static NotImplementedException noSuffix(@NonNull String s, @Nullable Object res) {
        return new NotImplementedException(res, s, false);
    }

    public NotImplementedException(@Nullable Object res, @NonNull String operation) {
        this(res, operation, true);
    }

    public NotImplementedException(@Nullable Object res, @NonNull String operation, boolean suffix) {
        super(operation + (suffix ? " is not implemented !" : ""));
        this.res = res;
    }
    
}
