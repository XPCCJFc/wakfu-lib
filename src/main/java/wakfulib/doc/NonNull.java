package wakfulib.doc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An element annotated with NonNull claims {@code null} value is <em>forbidden</em>
 * to return (for methods), pass to (parameters) and hold (local variables and fields).
 * Apart from documentation purposes this annotation is intended to be used by static analysis tools
 * to validate against probable runtime errors and element contract violations.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface NonNull {
}
