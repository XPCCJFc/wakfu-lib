package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent the value of the byte sent by clients (after the size)
 * supposedly used to indicate witch server to target
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ArchTarget.ArchTargets.class)
public @interface ArchTarget {
    byte value();
    String gameKey() default "universal";

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArchTargets {
        ArchTarget[] value();
    }

}
