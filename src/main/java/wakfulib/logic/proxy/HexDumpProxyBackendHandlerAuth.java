package wakfulib.logic.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.nio.ByteBuffer;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logic.OutPacket;
import wakfulib.logic.proxy.patch.PatchRegistry;

public class HexDumpProxyBackendHandlerAuth extends ReplayingDecoder<Void> {

    @Getter @Setter
    private PatchRegistry patchRegistry;
    private final Channel inboundChannel;
    private final IWakfulibLogger logManager;

    public HexDumpProxyBackendHandlerAuth(Channel inboundChannel, IWakfulibLogger logManager) {
        this.logManager = logManager;
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    /*
    * Server => Client
    */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        ByteBuffer adapt = adapt(buf, ctx);
        if (adapt != null) {
            logManager.log(Unpooled.wrappedBuffer(adapt), true);
            inboundChannel.writeAndFlush(Unpooled.wrappedBuffer(adapt)).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    public ByteBuffer adapt(ByteBuf buf, ChannelHandlerContext ctx) {
        final int msgSize = buf.readInt() & Integer.MAX_VALUE;
        final short msgType = buf.readShort();
        final byte[] rawData = new byte[msgSize - 6];
        buf.readBytes(rawData);
        var patchForPacket = patchRegistry.getPatchForPacket(msgType);
        if (patchForPacket != null) {
            OutPacket encode = patchForPacket.patchToClient(msgType, rawData);
            if (encode == null) {
                logManager.info("Packet (" + msgType + ") discarded by patch.");
                return null;
            }
            encode.finish();
            ByteBuf data = encode.getInternalBuffer();
            logManager.logEdited(data.duplicate(), true);
            inboundChannel.writeAndFlush(data).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
            return null;
        } else {
            final ByteBuffer bb = ByteBuffer.allocate(msgSize);
            bb.putInt((short)msgSize);
            bb.putShort(msgType);
            bb.put(rawData);
            bb.rewind();
            return bb;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        HexDumpProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logManager.error("> exceptionCaught()", cause);
        HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
        ctx.fireExceptionCaught(cause);
    }
}
