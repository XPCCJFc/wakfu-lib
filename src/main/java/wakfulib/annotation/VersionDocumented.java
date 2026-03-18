package wakfulib.annotation;

import wakfulib.internal.Version;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to document the versioning of a specific element.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface VersionDocumented {
    /**
     * The version in which this element was implemented.
     */
    Version implementVersion();

    /**
     * The last version in which this element was active.
     */
    Version lastVersion() default Version.UNKNOWN;

    /**
     * Whether this element has been tested.
     */
    boolean tested() default false;

    /**
     * The version in which this element was removed.
     */
    Version removedVersion() default Version.UNKNOWN;
}
