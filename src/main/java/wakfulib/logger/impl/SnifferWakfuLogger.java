package wakfulib.logger.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.LoggerFactory;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.model.SSLPacketHS;
import wakfulib.ui.proxy.model.WakfuPacket;

import java.nio.ByteBuffer;

public class SnifferWakfuLogger extends WakfulibLogAdapter {
    private final SnifferWindow snifferWindow;

    public SnifferWakfuLogger(SnifferWindow snifferWindow) {
        super(LoggerFactory.getLogger("SnifferLogManager"));
        this.snifferWindow = snifferWindow;
    }

    @Override
    public void log(ByteBuffer copy, boolean fromServer) {
        snifferWindow.incomingPacket(new WakfuPacket(Unpooled.wrappedBuffer(copy), fromServer));
    }

    @Override
    public void log(ByteBuf copy, boolean fromServer) {
        snifferWindow.incomingPacket(new WakfuPacket(copy, fromServer));
    }

    @Override
    public void logEdited(ByteBuf copy, boolean fromServer) {
        WakfuPacket packet = new WakfuPacket(copy, fromServer);
        packet.setEdited(true);
        snifferWindow.incomingPacket(packet);
    }

    @Override
    public void logSSlHS(boolean fromServer) {
        snifferWindow.incomingPacket(new SSLPacketHS(fromServer));
    }
}
