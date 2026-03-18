package wakfulib.internal.registration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes that are version-dependent.
 * Classes annotated with this will be automatically identified and registered
 * by the {@link VersionRegistry} during package scanning.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VersionDependant {
}
