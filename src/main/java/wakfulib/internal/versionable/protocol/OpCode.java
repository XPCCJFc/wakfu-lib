package wakfulib.internal.versionable.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import wakfulib.internal.Version;

/**
 * Associate a version to a unique identifier called opcode.
 * Used to annotate {@link wakfulib.internal.versionable.protocol.Message}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(OpCode.OpCodes.class)
public @interface OpCode {
    short value();
    Version version();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface OpCodes {
        OpCode[] value();
    }
}


