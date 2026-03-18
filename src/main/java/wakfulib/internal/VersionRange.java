package wakfulib.internal;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(VersionRange.VersionRanges.class)
public @interface VersionRange {
    Version min();
    Version max() default Version.UNKNOWN;

    @Retention(RetentionPolicy.RUNTIME)
    @interface VersionRanges {
        VersionRange[] value();
    }
}
