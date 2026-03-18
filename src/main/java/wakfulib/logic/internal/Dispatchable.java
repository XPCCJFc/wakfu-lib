package wakfulib.logic.internal;

import wakfulib.doc.Nullable;
import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;

public interface Dispatchable {
    default boolean isPoisonPill() {
        return false;
    }

    @Nullable
    Session getSession();

    void selfDispatch(EventManager eventManager);

    void release();
}
