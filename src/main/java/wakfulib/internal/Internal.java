package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DO NOT USE element annotated with this annotation, this could break the lib
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
}
