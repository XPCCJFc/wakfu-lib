package wakfulib.logic.internal;


import lombok.Getter;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.pipeline.DataNettyBuffer;
import wakfulib.utils.BufferUtils;

@Getter
public final class ScheduledMessage implements Dispatchable, Monitorable {
    private final boolean poisonPill = false;
    private final Session session;

    private final DataNettyBuffer byteBuf;
    private final Message<? extends Message<?>> template;

    public ScheduledMessage(Session session, DataNettyBuffer byteBuf, Message<? extends Message<?>> template) {
        this.session = session;
        this.byteBuf = byteBuf;
        this.template = template;
    }

    private Message<?> getMessage() {
        var mBuffer = byteBuf.getMBuffer();
        Message<?> message = template.unserialize(mBuffer);
        BufferUtils.errorIfRemaining(mBuffer);
        message.setOpCode(template.getOpCode());
        return message;
    }

    @Override
    public void selfDispatch(EventManager eventManager) {
        eventManager.dispatch(getMessage(), session);
    }

    @Override
    public void release() {
        byteBuf.release();
    }

    @Override
    public String[] getMonitoringLabel() {
        return new String[] {template.getClass().getSimpleName(), Integer.toString(template.getOpCode())};
    }
}
