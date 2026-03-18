package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark fields that will be injected by the framework.
 *
 * @apiNote Injected field can only be used (for now) in the {@link wakfulib.internal.versionable.protocol.Message#unserialize} methods
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
