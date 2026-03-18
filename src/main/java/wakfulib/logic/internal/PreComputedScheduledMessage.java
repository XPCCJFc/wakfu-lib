package wakfulib.logic.internal;


import lombok.Getter;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.pipeline.DataNettyBuffer;

@Getter
public final class PreComputedScheduledMessage implements Dispatchable, Monitorable {

    private final boolean poisonPill = false;
    private final Session session;
    private final Message<?> message;

    public PreComputedScheduledMessage(Session session, DataNettyBuffer dataNettyBuffer, Message<? extends Message<?>> template) {
        this.session = session;
        this.message = template.unserialize(dataNettyBuffer.getMBuffer());
        dataNettyBuffer.release();
        message.setOpCode(template.getOpCode());
    }

    @Override
    public void selfDispatch(EventManager eventManager) {
        eventManager.dispatch(message, session);
    }

    @Override
    public void release() {
        //noop
    }

    @Override
    public String[] getMonitoringLabel() {
        return new String[] {message.getClass().getSimpleName(), Integer.toString(message.getOpCode())};
    }
}
