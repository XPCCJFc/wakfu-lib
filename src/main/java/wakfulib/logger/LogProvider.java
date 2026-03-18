package wakfulib.logger;

import wakfulib.doc.NonNull;

@FunctionalInterface
public interface LogProvider {
    @NonNull
    IWakfulibLogger get(@NonNull Class<?> caller);
}
