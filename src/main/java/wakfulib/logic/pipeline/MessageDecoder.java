package wakfulib.logic.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import wakfulib.doc.NonNull;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.LogProvider;

import java.net.SocketException;
import java.util.List;

public class MessageDecoder extends ReplayingDecoder<MessagePacket> {
    private final IWakfulibLogger logManager;
    private final boolean isClient;
    private final int headerSize;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            logManager.error("- exceptionCaught(): {}", cause.getMessage());
        } else {
            logManager.error("- exceptionCaught(): ", cause);
        }
        ctx.close();
    }

    public MessageDecoder(@NonNull LogProvider logProvider, boolean isClient) {
        this.isClient = isClient;
        this.logManager = logProvider.get(MessageDecoder.class);
        this.headerSize = 2 + (isClient ? 0 : 1);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        var messageSizeFromHeader = in.readInt();
        logManager.debug("- decode(): reading a new message of size {}", messageSizeFromHeader);
        if (messageSizeFromHeader <= (isClient ? 0 : 1) || messageSizeFromHeader  > 50_000) throw new IllegalStateException("Invalid size is {} in the packet header: " + messageSizeFromHeader + " !");
        byte archCode = -1;
        if (! isClient) {
            archCode = in.readByte();
        }
        var byteBuf = in.readBytes(messageSizeFromHeader - headerSize);


        logManager.debug("- decode(): Message of size {} red successfully", messageSizeFromHeader);
        out.add(new MessagePacket(messageSizeFromHeader, archCode, new DataNettyBuffer(byteBuf)));
    }
}
