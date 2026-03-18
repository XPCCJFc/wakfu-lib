package wakfulib.logic;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.Internal;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.utils.ColorUtils;

/**
 * Represents an active network session between a client and a server.
 * Provides methods to send messages and manage session-specific data.
 * This class is intended to be extended for specific session types to store custom state.
 */
public abstract class Session implements Comparable<Session> {

    @Getter
    @Internal
    private final Channel channel;
    @Getter(AccessLevel.PACKAGE)
    @Internal
    private final IWakfulibLogger logManager;
    private final Map<String, Object> datas;

    /**
     * Creates a new session associated with the specified network channel.
     *
     * @param channel The network channel for this session.
     * @param logManager The logger for session-related events.
     */
    public Session(Channel channel, IWakfulibLogger logManager) {
        this.channel = channel;
        this.logManager = logManager;
        this.datas = new HashMap<>();
    }

    /**
     * Sends a protocol message to the remote peer.
     *
     * @param out The message to be sent.
     */
    public void send(@NonNull Message out) {
        if (channel == null) {
            return;
        }
        if (!channel.isOpen()) {
            logManager.warn("send({}: {})Channel is closed, cannot send packet", out.getOpCode(), out.getClass().getSimpleName());
            return;
        }
        OutPacket encode = out.encode();
        log(out, encode);
        channel.writeAndFlush(encode);
    }

    /**
     * Closes the session and the underlying network connection.
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
        datas.clear();
    }

    /**
     * Associates custom data with the session using a key.
     *
     * @param key The data identifier.
     * @param data The data object to store.
     * @deprecated Use a custom {@link Session} subclass and the session factory in {@link WakfuClientConnection.WakfuClientConnectionBuilder} to manage session-specific state.
     */
    @Deprecated(forRemoval = true)
    public void addData(@NonNull String key, @Nullable Object data) {
        datas.put(key, data);
    }

    /**
     * Removes and returns custom data from the session.
     *
     * @param key The data identifier.
     * @return The removed data object, or {@code null} if not found.
     * @deprecated Use a custom {@link Session} subclass and the session factory in {@link WakfuClientConnection.WakfuClientConnectionBuilder} to manage session-specific state.
     */
    @Deprecated(forRemoval = true)
    @Nullable
    public <T> T removeData(String key) {
        return (T) datas.remove(key);
    }

    /**
     * Retrieves custom data associated with the session.
     *
     * @param key The data identifier.
     * @return The data object, or {@code null} if not found.
     * @deprecated Use a custom {@link Session} subclass and the session factory in {@link WakfuClientConnection.WakfuClientConnectionBuilder} to manage session-specific state.
     */
    @Deprecated(forRemoval = true)
    @Nullable
    public <T> T getData(String key) {
        return (T) datas.get(key);
    }

    /**
     * Logs an outgoing message and its encoded packet.
     *
     * @param out The message that was sent.
     * @param encode The encoded packet data.
     */
    protected void log(@NotNull Message out, OutPacket encode) {
        if (logManager != null) {
            logManager.info(ColorUtils.ANSI_PURPLE + "Outgoing packet: ({}) [op:{}, size: {}]" + ColorUtils.ANSI_RESET,
                out.getClass().getSimpleName(), out.getOpCode(), encode.writerIndex() - encode.readerIndex());
            if (logManager.isTraceEnabled()) {
                encode.finish();
                logManager.debug(encode.getBuffer(true));
            }
        }
    }

    /**
     * Compares this session with another session for order.
     * Comparison is based on the underlying network channel.
     *
     * @param o The session to be compared.
     * @return A negative integer, zero, or a positive integer as this session
     *         is less than, equal to, or greater than the specified session.
     */
    @Override
    public final int compareTo(@NotNull Session o) {
        if (channel == null) {
            if (o.channel == null) {
                return 0;
            } else {
                return -1;
            }
        }
        return channel.compareTo(o.channel);
    }

    /**
     * Indicates whether some other object is "equal to" this session.
     * Equality is based on the underlying network channel.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this session is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Session session = (Session) o;
        if (channel == null) {
            return session.channel == null;
        }

        return channel.compareTo(session.channel) == 0;
    }

    /**
     * Returns a hash code value for the session.
     *
     * @return A hash code value for this session.
     */
    @Override
    public final int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }
}
