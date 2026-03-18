package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Utility annotation to help the packet view understand the format of a string<br>
 *
 * <b>Only String fields should be annotated with Structure</b>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Structure {

    StringSize stringSize();

    enum StringSize {
        BYTE,
        SHORT,
        INTEGER,
        ;
    }
}
