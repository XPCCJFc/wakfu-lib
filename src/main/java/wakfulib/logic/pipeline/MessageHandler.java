package wakfulib.logic.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Setter;
import wakfulib.doc.NonNull;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.internal.versionable.protocol.ToServerMessage;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.LogProvider;
import wakfulib.logic.Session;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.event.def.ConnectionClosedEvent;
import wakfulib.logic.event.def.ConnectionEstablishedEvent;
import wakfulib.logic.internal.Dispatchable;
import wakfulib.logic.internal.DispatchableEvent;
import wakfulib.logic.internal.MessageDispatcher;
import wakfulib.logic.internal.ScheduledMessage;
import wakfulib.utils.ColorUtils;
import wakfulib.utils.data.TriFunction;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private static long READER_IDLE_TIME_NANOS = 1_000_000_000L * 60 * 10; // 10min

    public static final AttributeKey<Session> CLIENTSESS_ATTR = AttributeKey.newInstance("ClientSession");
    public static final ChannelGroup ALL_CLIENTS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public final Map<Integer, Message> INCOMING_PACKET_MAP = new ConcurrentHashMap<>();
    private final IWakfulibLogger logManager;
    private final EventManager eventManager;
    private final boolean isClient;
    @Setter
    private TriFunction<Session, DataNettyBuffer, Message<? extends Message<?>>, Dispatchable> messageFactory = ScheduledMessage::new;
    private final BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory;
    private final TransferQueue<Dispatchable> queueRef;
    private Future<?> readerIdleTimeout;
    private long lastReadTime;
    private boolean reading;
    private Boolean init;

    public MessageHandler(@NonNull EventManager eventManager, @NonNull LogProvider logProvider,
                          boolean isClient, BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory,
                          MessageDispatcher messageDispatcher) {
        this.init = false;
        this.logManager = logProvider.get(MessageHandler.class);
        this.isClient = isClient;
        this.eventManager = eventManager;
        this.sessionFactory = sessionFactory;
        this.queueRef = messageDispatcher.getMessageQueue();
        initPacketsMap();
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logManager.debug("Channel active, session created");
        Channel channel = ctx.channel();
        if (! isClient) {
            ALL_CLIENTS.add(channel);
        }
        this.initialize(ctx);
        Session sess = sessionFactory.apply(channel, logManager);
        ctx.channel().attr(CLIENTSESS_ATTR).setIfAbsent(sess);
        eventManager.setSessionFields(sess);
        super.channelActive(ctx);
        queueRef.transfer(new DispatchableEvent(sess, new ConnectionEstablishedEvent()));
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            this.initialize(ctx);
        }
    }

    private void initialize(ChannelHandlerContext ctx) {
        if (this.init == Boolean.FALSE) {
            init = true;
            if (this.READER_IDLE_TIME_NANOS > 0L) {
                this.lastReadTime = this.ticksInNanos();
                this.readerIdleTimeout = this.schedule(ctx, new ReaderIdleTimeoutTask(ctx), this.READER_IDLE_TIME_NANOS, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logManager.debug("Channel inactive, session deleted");
        Session session = ctx.channel().attr(CLIENTSESS_ATTR).get();
        this.destroy();
        super.channelInactive(ctx);
        queueRef.transfer(new DispatchableEvent(session, new ConnectionClosedEvent()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            logManager.error("- exceptionCaught(): {}", cause.getMessage());
        } else {
            logManager.error("- exceptionCaught(): ", cause);
        }
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (this.READER_IDLE_TIME_NANOS > 0L) {
            this.reading = true;
        }

        MessagePacket packet = (MessagePacket) msg;
        var bufferwrapper = packet.buffer();
        var buffer = bufferwrapper.getMBuffer();
        var opcode = (int) buffer.getShort();
        Message serverMessage = INCOMING_PACKET_MAP.get(opcode);
        if (INCOMING_PACKET_MAP.containsKey(opcode)) {
            logManager.info(ColorUtils.ANSI_CYAN + "Incoming packet: ({}) [op: {}, size: {}]" + ColorUtils.ANSI_RESET, serverMessage.getClass().getSimpleName(),
                opcode, packet.size());
            if (logManager.isTraceEnabled()) {
                logManager.log(buffer.duplicate(), false);
            }
            Session session = ctx.channel().attr(CLIENTSESS_ATTR).get();
            try {
                queueRef.transfer(messageFactory.apply(session, bufferwrapper, serverMessage));
            } catch (InterruptedException e) {
                logManager.error(ColorUtils.ANSI_RED + "Failed to send a message with op (" + opcode + ") as queue is busy !" + ColorUtils.ANSI_RESET);
            }

        } else {
            logManager.warn(ColorUtils.ANSI_BLUE + "Incoming packet: (Unknown) [op: " + opcode + ", size: " + packet.size() + "]" + ColorUtils.ANSI_RESET);
        }
    }

    private Future<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }

    private long ticksInNanos() {
        return System.nanoTime();
    }

    private void destroy() {
        this.init = null;
        if (this.readerIdleTimeout != null) {
            this.readerIdleTimeout.cancel(false);
            this.readerIdleTimeout = null;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (READER_IDLE_TIME_NANOS > 0L && this.reading) {
            this.lastReadTime = this.ticksInNanos();
            this.reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    public static void setTimeout(long timeoutNs) {
        READER_IDLE_TIME_NANOS = timeoutNs;
    }

    @AllArgsConstructor
    private final class ReaderIdleTimeoutTask implements Runnable {
        private ChannelHandlerContext ctx;

        public void run() {
            if (! this.ctx.channel().isOpen()) return;
            long nextDelay = READER_IDLE_TIME_NANOS;
            if (! MessageHandler.this.reading) {
                nextDelay -= MessageHandler.this.ticksInNanos() - MessageHandler.this.lastReadTime;
            }

            if (nextDelay <= 0L) {
                logManager.info("- No packet for " + READER_IDLE_TIME_NANOS + "ns closing connecting...");
                this.ctx.channel().close();
            } else {
                MessageHandler.this.readerIdleTimeout = MessageHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }

        }
    }

    private void initPacketsMap() {
        for (Object value : VersionRegistry.registeredClasses()) {
            if (isClient) {
                if (value instanceof ToClientMessage) {
                    ToClientMessage tcMessage = (ToClientMessage) value;
                    INCOMING_PACKET_MAP.put(tcMessage.getOpCode(), tcMessage);
                }
            } else {
                if (value instanceof ToServerMessage) {
                    ToServerMessage tcMessage = (ToServerMessage) value;
                    INCOMING_PACKET_MAP.put(tcMessage.getOpCode(), tcMessage);
                }
            }
        }
    }
}
