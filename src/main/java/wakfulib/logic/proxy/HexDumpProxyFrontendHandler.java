package wakfulib.logic.proxy;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logic.OutPacket;
import wakfulib.logic.proxy.patch.Patch;
import wakfulib.logic.proxy.patch.PatchRegistry;

public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;
    private final Supplier<ChannelInitializer<SocketChannel>> backendInitializer;
    private final IWakfulibLogger logManager;
    @Getter @Setter
    private PatchRegistry patchRegistry;

    // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    private Channel outboundChannel;

    public HexDumpProxyFrontendHandler(String remoteHost, int remotePort, Supplier<ChannelInitializer<SocketChannel>> backendInitializer,
                                       IWakfulibLogger logManager) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.backendInitializer = backendInitializer;
        this.logManager = logManager;
    }

    /*
     * Client => Server
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        logManager.debug("Channel active: creating a new connection to " + remoteHost + ":" + remotePort + " !");
        b.group(inboundChannel.eventLoop())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
            .channel(ctx.channel().getClass())
            .handler(backendInitializer.get())
            .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logManager.info("Connected to " + remoteHost + ":" + remotePort + " !");
                // connection complete start to read first data
                inboundChannel.read();
            } else {
                logManager.warn("Failed to connect to " + remoteHost + ":" + remotePort + " !");
                // Close the connection if the connection attempt has failed.
                inboundChannel.close();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof ByteBuf buf) {
                ByteBuf copy = buf.duplicate();
                while (copy.readableBytes() >= 7) {
                    int size = copy.readInt();
                    byte archTarget = copy.readByte();
                    short id = copy.readShort();
//                if (copy.readShort() == 5635 && archTarget == 3 && id == 10) {
//                    logManager.info("SSL detected !");
//                    logManager.logSSlHS(false);
//                } else {
                    var remainingSize = Math.min(copy.writerIndex() - copy.readerIndex(), size - 7);
                    var sliced = copy.copy(copy.readerIndex() - 7, remainingSize + 7);
                    copy.readerIndex(copy.readerIndex() + remainingSize);

                    Patch patch = patchRegistry.getPatchForPacket(id);
                    if (patch != null) {
                        final byte[] rawData = new byte[remainingSize];
                        var initialReaderIndex = sliced.readerIndex();
                        sliced.readerIndex(sliced.readerIndex() + 7);
                        sliced.readBytes(rawData);
                        OutPacket outPacket = patch.patchToServer(archTarget, id, rawData);
                        if (outPacket == null) {
                            sliced.readerIndex(initialReaderIndex);
                            logManager.log(sliced, false);
                            logManager.info("Packet (" + id + ") discarded by patch.");
                            return;
                        }
                        outPacket.finish();
                        msg = outPacket.getInternalBuffer();
                        logManager.logEdited(outPacket.getInternalBuffer().duplicate(), false);
//                    } else {
//                        logManager.log(sliced, false);
//                    }
                }
                }
            }
        } catch (Exception e) {
            logManager.error("Error while reading data from HexDumpProxyFrontendHandler", e);
        }
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            logManager.error("INACTIVE WTFFFF");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logManager.error("Error caught in HexDumpProxyFrontendHandler", cause);
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
