package wakfulib.logic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.LogProvider;
import wakfulib.logger.impl.WakfulibLogAdapter;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.internal.MessageDispatcher;
import wakfulib.logic.pipeline.MessageDecoder;
import wakfulib.logic.pipeline.MessageHandler;
import wakfulib.logic.pipeline.PacketEncoder;
import wakfulib.logic.ssl.SSLContextHolder;

import java.util.function.BiFunction;

/**
 * Represents a server connection that listens for incoming Wakfu client connections.
 * Use {@link WakfuServerConnectionBuilder} to configure and start the server.
 */
@Slf4j
public class WakfuServerConnection {

    /**
     * Builder for {@link WakfuServerConnection}.
     * Allows configuring the listening port, SSL settings, and handlers.
     */
    public static class WakfuServerConnectionBuilder {
        boolean ssl = false;
        private final EventManager eventManager;
        private int port;
        private LogProvider logProvider = WakfulibLogAdapter::getLogger;
        private BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory = DummySession :: new;
        private Class<? extends ServerChannel> socketClass = NioServerSocketChannel.class;
        private EventLoopGroup parentGroup;
        private EventLoopGroup childGroup;
        private EventLoopGroup msgGroup;
        private MessageDispatcher messageDispatcher;

        /**
         * Creates a new builder with the specified event manager.
         * The event manager will be used to dispatch network events for all connected clients.
         *
         * @param eventManager The event manager to use.
         */
        public WakfuServerConnectionBuilder(@NonNull EventManager eventManager) {
            this.eventManager = eventManager;
        }

        /**
         * Configures custom I/O provider settings for the underlying network library.
         *
         * @param socketClass The server socket channel class to use (e.g., NioServerSocketChannel.class).
         * @param parent The parent event loop group for accepting connections.
         * @param child The child event loop group for handling client I/O.
         * @param msgGroup The event loop group for message processing.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withIOProvider(Class<? extends ServerChannel> socketClass, EventLoopGroup parent, EventLoopGroup child, EventLoopGroup msgGroup) {
            this.socketClass = socketClass;
            this.parentGroup = parent;
            this.childGroup = child;
            this.msgGroup = msgGroup;
            return this;
        }

        /**
         * Enables SSL/TLS for the server to secure incoming connections.
         *
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withSSL() {
            this.ssl = true;
            return this;
        }

        /**
         * Configures the server to use an already started message dispatcher.
         * This is useful when multiple server instances share the same dispatcher.
         *
         * @param messageDispatcher The message dispatcher to use.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withStartedMessageDispatcher(MessageDispatcher messageDispatcher) {
            this.messageDispatcher = messageDispatcher;
            return this;
        }

        /**
         * Configures whether to use SSL/TLS for the server.
         *
         * @param ssl {@code true} to enable SSL, {@code false} to disable it.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withSSL(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        /**
         * Configures the local port the server will listen on.
         *
         * @param port The port to listen on.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder bind(int port) {
            this.port = port;
            return this;
        }

        /**
         * Configures the logger provider to use for server and session events.
         *
         * @param logProvider The provider that returns a logger instance.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withLogger(LogProvider logProvider) {
            this.logProvider = logProvider;
            return this;
        }

        /**
         * Configures the session factory to use for creating new client session objects.
         * A custom session factory allows creating subclasses of {@link Session} to store client-specific state.
         *
         * @param sessionFactory A function that creates a {@link Session} from a channel and a logger.
         * @return This builder instance for chaining.
         */
        public WakfuServerConnectionBuilder withSessionFactory(@NonNull BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }

        /**
         * Starts the server listener and blocks until the server is shut down.
         *
         * @throws Exception if an error occurs during server startup or while it is running.
         */
        public void start() throws Exception {
            if (this.sessionFactory == null) throw new IllegalStateException("You must set an address before calling the start method, either by calling the bind(...) or the localhost(...) methods.");
            if (this.port < 0) throw new IllegalStateException("The port specified '" + port + "' is invalid (must be > 0)");

            final var finalSessionFactory = sessionFactory;

            final var encoder = new PacketEncoder(logProvider);
            if (messageDispatcher == null) {
                messageDispatcher = new MessageDispatcher(eventManager);
            }
            final var dispatcher = messageDispatcher;
            dispatcher.runAsDeamon();

            final EventLoopGroup messageHandlerExecutor = msgGroup == null ? new NioEventLoopGroup(1, new DefaultThreadFactory("nioIn", Thread.MAX_PRIORITY)) : msgGroup;

            ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(
                    parentGroup == null ? new NioEventLoopGroup(1, new DefaultThreadFactory("nioM", Thread.MAX_PRIORITY)) : parentGroup,
                    childGroup == null ? new NioEventLoopGroup(new DefaultThreadFactory("nioC", Thread.MAX_PRIORITY)) : childGroup
                )
                .channel(socketClass)
                .localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        var pipeline = sc.pipeline();
                        if (ssl) {
                            pipeline.addLast(SSLContextHolder.forServer().newHandler(sc.alloc()));
                        }
                        pipeline.addLast(new MessageDecoder(logProvider, false));
                        pipeline.addLast(messageHandlerExecutor, new MessageHandler(eventManager, logProvider, false, finalSessionFactory, dispatcher));
                        pipeline.addLast(encoder);
                    }
                });
            serverBootstrap.bind()
                .awaitUninterruptibly()
                .channel().closeFuture().awaitUninterruptibly();
        }
    }
}
