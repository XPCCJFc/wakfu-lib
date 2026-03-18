package wakfulib.logic.internal;

import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;

public interface RunnableDispatchable extends Dispatchable, Runnable {
    default Session getSession() {
        return null;
    }

    @Override
    default void release() {

    }

    @Override
    default void selfDispatch(EventManager eventManager) {
        run();
    }
}
