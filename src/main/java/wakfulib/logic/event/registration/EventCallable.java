package wakfulib.logic.event.registration;

import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logic.Session;
import wakfulib.logic.event.Event;

import java.lang.invoke.MethodHandle;

public interface EventCallable {
    void call(@Nullable Session session, @NonNull Event message);

    int getPriority();

    MethodHandle getHandle();
}
