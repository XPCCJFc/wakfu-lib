package wakfulib.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an element (method return value, parameter, or field) cannot be null.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface NonNull {
}
