package wakfulib.logic;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Iterator;
import java.util.List;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.Internal;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.utils.ColorUtils;

public abstract class SessionMultiplexer implements ISessionMultiplexer {
    @Internal
    private final ChannelGroup group;
    @Setter
    private static boolean trackFailure = false;
    protected final IWakfulibLogger logManager;
    @Setter
    protected static IWakfulibLogger staticLogManager;

    public SessionMultiplexer(@Nullable IWakfulibLogger logManager) {
        this.logManager = logManager;
        if (logManager != null) {
            group = new DefaultChannelGroup(logManager.getName(), GlobalEventExecutor.INSTANCE);
        } else {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        }
    }

    public Iterator<Channel> iterate() {
        return group.iterator();
    }

    public void add(Session session) {
        if (session.getChannel() != null) {
            group.add(session.getChannel());
        }
    }

    public void remove(Session session) {
        if (session.getChannel() != null) {
            group.remove(session.getChannel());
        }
    }

    public int size() {
        return group.size();
    }

    public void broadcast(@NonNull Message out) {
        OutPacket encode = out.encode();
        log(logManager, out, encode, "Broadcast", group.size());
        var channelFutures = group.writeAndFlush(encode);
        if (trackFailure) {
            trackFailures(channelFutures);
        }
    }

    public void broadcastExcept(@NonNull Message out, @NonNull Session session) {
        OutPacket encode = out.encode();
        ChannelGroupFuture channelFutures;
        if (session.getChannel() == null) {
            log(logManager, out, encode, "Broadcast except nil", group.size());
            channelFutures = group.writeAndFlush(encode);
        } else {
            log(logManager, out, encode, "Broadcast except one", group.size());
            channelFutures = group.writeAndFlush(encode, c -> !c.equals(session.getChannel()));
        }
        if (trackFailure) {
            trackFailures(channelFutures);
        }
    }

    public static void broadcast(@NotNull List<Session> sessions, @NonNull Message out) {
        OutPacket encode = out.encode();
        log(staticLogManager, out, encode, "Static broadcast packet", sessions.size());
        for (Session session : sessions) {
            var channel = session.getChannel();
            if (channel == null) continue;
            var channelFuture = channel.writeAndFlush(encode);
            if (trackFailure && session.getLogManager() != null) {
                channelFuture.addListener(future -> {
                    if (!future.isSuccess()) {
                        if (staticLogManager != null) {
                            staticLogManager.debug("PartialFailure !");
                        }
                    }
                });
            }
        }
    }

    private void trackFailures(ChannelGroupFuture channelFutures) {
        channelFutures.addListener((ChannelGroupFutureListener) future -> {
            if (!future.isSuccess()) {
                logManager.error("PartialFailure !");
            }
        });
    }

    private static void log(@Nullable IWakfulibLogger logManager, @NotNull Message out, @NonNull OutPacket encode, String type, int groupSize) {
        if (logManager != null) {
            logManager.info(ColorUtils.ANSI_PURPLE + "{} packet: ({}) (op:{}, size: {}) to {} client(s)" + ColorUtils.ANSI_RESET, type,
                out.getClass().getSimpleName(), out.getOpCode(), encode.writerIndex() - encode.readerIndex(), groupSize);
            if (logManager.isTraceEnabled()) {
                encode.finish();
                logManager.trace(encode.getBuffer(true));
            }
        }
    }
}
