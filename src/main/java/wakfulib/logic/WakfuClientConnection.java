package wakfulib.logic;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetSocketAddress;
import java.util.function.BiFunction;
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

/**
 * Represents a client connection to a Wakfu server.
 * Use {@link WakfuClientConnectionBuilder} to configure and start the connection.
 */
public class WakfuClientConnection {

    /**
     * Builder for {@link WakfuClientConnection}.
     * Allows configuring the connection address, port, SSL settings, and handlers.
     */
    public static class WakfuClientConnectionBuilder {
        boolean ssl = false;
        private final EventManager eventManager;
        private String address;
        private int port;
        private EventLoopGroup group = null;
        private LogProvider logProvider = WakfulibLogAdapter::getLogger;
        private Class<? extends SocketChannel> socketClass = NioSocketChannel.class;
        private BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory = DummySession :: new;

        /**
         * Creates a new builder with the specified event manager.
         * The event manager will be used to dispatch network events to registered handlers.
         *
         * @param eventManager The event manager to use.
         */
        public WakfuClientConnectionBuilder(@NonNull EventManager eventManager) {
            this.eventManager = eventManager;
        }

        /**
         * Configures custom I/O provider settings for the underlying network library.
         *
         * @param socketClass The socket channel class to use (e.g., NioSocketChannel.class).
         * @param group The event loop group to use for handling I/O events.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder withIOProvider(Class<? extends SocketChannel> socketClass, EventLoopGroup group) {
            this.socketClass = socketClass;
            this.group = group;
            return this;
        }

        /**
         * Enables SSL/TLS for the connection.
         *
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder withSSL() {
            this.ssl = true;
            return this;
        }

        /**
         * Configures whether to use SSL/TLS for the connection.
         *
         * @param ssl {@code true} to enable SSL, {@code false} to disable it.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder withSSL(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        /**
         * Configures the connection to use "localhost" on the specified port.
         *
         * @param port The port to connect to.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder localHost(int port) {
            this.address = "localhost";
            this.port = port;
            return this;
        }

        /**
         * Configures the connection to use the specified remote address and port.
         *
         * @param address The remote address to connect to.
         * @param port The remote port to connect to.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder bind(@NonNull String address, int port) {
            this.address = address;
            this.port = port;
            return this;
        }

        /**
         * Configures the logger provider to use for connection and session events.
         *
         * @param logProvider The provider that returns a logger instance.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder withLogger(@NonNull LogProvider logProvider) {
            this.logProvider = logProvider;
            return this;
        }

        /**
         * Configures the session factory to use for creating new session objects.
         * A custom session factory allows creating subclasses of {@link Session} to store custom state.
         *
         * @param sessionFactory A function that creates a {@link Session} from a channel and a logger.
         * @return This builder instance for chaining.
         */
        public WakfuClientConnectionBuilder withSessionFactory(@NonNull BiFunction<Channel, IWakfulibLogger, ? extends Session> sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }

        /**
         * Starts the client connection and blocks until the connection is closed.
         *
         * @throws Exception if an error occurs during connection setup or while the connection is active.
         */
        public void start() throws Exception {
            if (this.address == null) throw new IllegalStateException("You must set an address before calling the start method, either by calling the bind(...) or the localhost(...) methods.");
            if (this.sessionFactory == null) throw new IllegalStateException("You must set an address before calling the start method, either by calling the bind(...) or the localhost(...) methods.");
            if (this.port < 0) throw new IllegalStateException("The port specified '" + port + "' is invalid (must be > 0)");

            var messageDispatcher = new MessageDispatcher(eventManager);

            final var finalSessionFactory = sessionFactory;
            if (group == null) {
                group = new NioEventLoopGroup(new DefaultThreadFactory("nioGrp", Thread.MAX_PRIORITY));
            }
            try {
                messageDispatcher.runAsDeamon();
                Bootstrap clientBootstrap = new Bootstrap();
                clientBootstrap.group(group);
                clientBootstrap.channel(socketClass);
                clientBootstrap.remoteAddress(new InetSocketAddress(address, port));
                clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        var pipeline = socketChannel.pipeline();
                        if (ssl) {
                            pipeline.addLast(SSLContextHolder.forClient().newHandler(socketChannel.alloc()));
                        }
                        pipeline.addLast(new MessageDecoder(logProvider, true));
                        pipeline.addLast(new MessageHandler(eventManager, logProvider, true, finalSessionFactory, messageDispatcher));
                        pipeline.addLast(new PacketEncoder(logProvider));
                    }
                });
                ChannelFuture channelFuture = clientBootstrap.connect().sync();
                channelFuture.channel().closeFuture().sync();
            } finally {
                group.shutdownGracefully().sync();
            }
        }
    }
}
