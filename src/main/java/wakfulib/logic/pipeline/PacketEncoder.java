package wakfulib.logic.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import wakfulib.doc.NonNull;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.LogProvider;
import wakfulib.logic.OutPacket;

@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<OutPacket> {

    private final IWakfulibLogger logManager;

    public PacketEncoder(@NonNull LogProvider provider) {
        super(OutPacket.class);
        this.logManager = provider.get(PacketEncoder.class);
    }

    protected void encode(@NonNull ChannelHandlerContext ctx, @NonNull OutPacket msg, @NonNull ByteBuf out) {
        msg.finish();
        ByteBuf data = msg.getInternalBuffer().duplicate();//duplicate cause of the multiplexer
        if (logManager != null) {
            try {
                ByteBuf copy = data.duplicate();
                logManager.log(copy, false);
            } catch (Exception e) {
                logManager.error("Error while logging packet (op: {})", msg.getPacketId(), e);
            }
        }
        out.writeBytes(data);
        ctx.channel().flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logManager != null) {
            logManager.error("Error caught in PacketEncoder", cause);
        }
    }
}
