package wakfulib.logic;

import io.netty.channel.Channel;
import wakfulib.logger.IWakfulibLogger;

/**
 * A basic implementation of {@link Session} that provides standard session
 * functionality without additional custom data or specialized logic.
 */
public class DummySession extends Session {
    /**
     * Creates a new dummy session associated with the specified network channel.
     *
     * @param channel The network channel for this session.
     * @param logManager The logger for session-related events.
     */
    public DummySession(Channel channel, IWakfulibLogger logManager) {
        super(channel, logManager);
    }
}
