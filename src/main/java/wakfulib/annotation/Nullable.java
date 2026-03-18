package wakfulib.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an element (method return value, parameter, or field) can be null.
 * Elements annotated with this should be checked for null before use.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Nullable {
}
