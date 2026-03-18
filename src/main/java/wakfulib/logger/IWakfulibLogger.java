package wakfulib.logger;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

/**
 * Specialized logger interface for WakfuLib.
 * Extends the standard SLF4J {@link Logger} with methods for logging
 * raw network packet data and SSL handshake status.
 */
public interface IWakfulibLogger extends Logger {

    /**
     * Logs raw packet data from a {@link ByteBuffer}.
     *
     * @param original The buffer containing the raw data.
     * @param fromServer Whether the data originated from the server.
     */
    void log(ByteBuffer original, boolean fromServer);

    /**
     * Logs raw packet data from a {@link ByteBuf}.
     *
     * @param copy The buffer containing the raw data.
     * @param fromServer Whether the data originated from the server.
     */
    void log(ByteBuf copy, boolean fromServer);

    /**
     * Logs edited packet data.
     *
     * @param copy The buffer containing the edited data.
     * @param fromServer Whether the data originated from the server.
     */
    void logEdited(ByteBuf copy, boolean fromServer);

    /**
     * Logs the status of an SSL handshake.
     *
     * @param b Whether the handshake was successful.
     */
    void logSSlHS(boolean b);
}
