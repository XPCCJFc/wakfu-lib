package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow the annotated {@link wakfulib.internal.versionable.protocol.Message}
 * to register with an opcode that is already used by another Message.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicateOpCode {
}
