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

public class HexDumpProxyBackendHandlerWorld extends ReplayingDecoder<Void> implements ChannelProvider {

    @Getter @Setter
    private PatchRegistry patchRegistry;
    private final Channel inboundChannel;
    private final IWakfulibLogger logManager;

    public HexDumpProxyBackendHandlerWorld(Channel inboundChannel, IWakfulibLogger logManager) {
        this.inboundChannel = inboundChannel;
        this.logManager = logManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.fireChannelActive();
    }

    /*
     * Server => Client
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        final ByteBuf adapt = adapt(buf, ctx);
        if (adapt != null) {
            logManager.log(adapt.copy(), true);
            inboundChannel.writeAndFlush(adapt).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    logManager.error("Sending message failed", future.cause());
                    future.channel().close();
                }
            });
        }
    }

    public ByteBuf adapt(ByteBuf buf, ChannelHandlerContext ctx) {
        buf.markReaderIndex();
        final int msgSize = buf.readInt() & 0xFFFF;
        final short msgType = buf.readShort();
        if (msgSize == 0 && msgType == 10) {
            buf.resetReaderIndex();
            final ByteBuffer bb = ByteBuffer.allocate(buf.writerIndex() - buf.readerIndex());
            final byte[] rawData = new byte[buf.writerIndex() - buf.readerIndex()];
            buf.readBytes(rawData);
            bb.put(rawData);
            bb.rewind();
            return Unpooled.wrappedBuffer(bb);
        }

        final byte[] rawData = new byte[msgSize - 4 - 2];
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
            logManager.logEdited(data.copy(), true);
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
            return Unpooled.wrappedBuffer(bb);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        HexDumpProxyFrontendHandler.closeOnFlush(inboundChannel);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logManager.error("> exceptionCaught()", cause);
        HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public Channel getChannel() {
        return inboundChannel;
    }
}
