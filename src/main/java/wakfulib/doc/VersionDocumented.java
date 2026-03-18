package wakfulib.doc;

import wakfulib.internal.Version;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface VersionDocumented {
    Version implementVersion();
    Version lastVersion() default Version.UNKNOWN;
    boolean tested() default false;
    Version removedVersion() default Version.UNKNOWN;
}
