package wakfulib.logic.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.event.InternalEvent;

@Getter
@AllArgsConstructor
public class DispatchableEvent implements Dispatchable, Monitorable {
    private final Session session;
    public final InternalEvent event;

    @Override
    public void selfDispatch(EventManager eventManager) {
        eventManager.dispatch(event, session);
    }

    @Override
    public void release() {
        //noop
    }

    @Override
    public String[] getMonitoringLabel() {
        return new String[] {event.getClass().getSimpleName(), "-1"};
    }
}
