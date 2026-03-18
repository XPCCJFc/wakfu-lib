package wakfulib.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a class to render the type annotated with this enum instead of the default
 * renderer.
 *
 * The viewer class must implement a public static method called getView with an unique Object argument;
 * the return type of the method can be a String or a {@link wakfulib.ui.utils.MutliLineString}
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
    Class<?> viewer();
    boolean inline() default false;
    String name() default "no name";
}
