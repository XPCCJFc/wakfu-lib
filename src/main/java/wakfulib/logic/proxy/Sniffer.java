package wakfulib.logic.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.impl.WakfulibLogAdapter;
import wakfulib.logic.proxy.patch.PatchRegistry;
import wakfulib.logic.ssl.SSLContextHolder;
import wakfulib.ui.proxy.SnifferLauncher;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.conf.IConfiguration;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.utils.data.Triplet;

@Slf4j
public abstract class Sniffer implements AutoCloseable {
    public static IWakfulibLogger logManager;

    protected static IConfiguration configuration;

    public final AtomicReference<Runnable> endWorld = new AtomicReference<>();
    public final AtomicReference<Runnable> endAuth = new AtomicReference<>();

    public final WakfuConnectionChannels wakfuConnectionChannels = new WakfuConnectionChannels();

    public static void launchSnifferWithEditableConfiguration(@NonNull SnifferStarter starter) {
        SnifferLauncher snifferLauncher = new SnifferLauncher();
        int res = JOptionPane.showConfirmDialog(null, snifferLauncher, "[Sniffer] Configure the sniffer", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            SwingUtilities.invokeLater(() -> launchSniffer(snifferLauncher.selectedOptionAsConfiguration(), starter));
        } else {
            log.info("[Sniffer] Goodbye");
        }
    }

    public static void launchSniffer(@NonNull IConfiguration configuration, @NonNull SnifferStarter starter) {
        log.info("Starting Sniffer...");
        Sniffer.configuration = configuration;

        Options options = Settings.getInstance().getOptions();
        boolean hideAuth;
        if (! options.isREMEMBER_AUTH_PRIVACY_OPTION()) {
            JCheckBox checkbox = new JCheckBox("Do not show this message again.");
            String message = "Would you like to hide auths messages";
            Object[] params = {message, checkbox};
            hideAuth = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, params, "Hide auth ?", JOptionPane.YES_NO_OPTION);
            if (checkbox.isSelected()) {
                options.setREMEMBER_AUTH_PRIVACY_OPTION(true);
                options.setHIDE_AUTH(hideAuth);
            }
        }
        try {
            Settings.getInstance().saveToFile();
        } catch (Exception e) {
            log.warn("Couldn't save settings", e);
        }
        Triplet<Supplier<SnifferWindow>, Supplier<Sniffer>, BiConsumer<Sniffer, SnifferWindow>> init = starter.init(configuration);
        Sniffer sniffer = init._2.get();
        SwingUtilities.invokeLater(() -> {
            SnifferWindow snifferWindow = init._1.get();
            snifferWindow.setOnExit(sniffer::close);
            new Thread(() -> init._3.accept(sniffer, snifferWindow)).start();
        });
    }

    public void start(SnifferWindow window, @Nullable Consumer<PatchRegistry> authPatches, @Nullable Consumer<PatchRegistry> worldPatches) {
        if (logManager == null) logManager = window.getLogManager();
        getLogManager();

        window.setWakfuConnectionChannels(wakfuConnectionChannels);
        window.setOnExit(this::close);
        if (! configuration.isOnlyWorld()) {
            log.info("[Auth] Proxying 127.0.0.1:" + configuration.getLocalAuthPort() + " to " + configuration.getRemoteAuthAddress() + ':' + configuration.getRemoteAuthPort() + " ...");
            new Thread(() -> {
                try {
                    log.info("[Auth] Sniffer auth starting");
                    snifferAuth(authPatches);
                } catch (Exception e) {
                    log.error("[Auth]", e);
                }
            }).start();
            log.info("[Auth] Sniffer world starting");
        }
        try {
            log.info("[World] Proxying 127.0.0.1:" + configuration.getLocalWorldPort() + " to " + configuration.getRemoteWorldAddress() + ':' + configuration.getRemoteWorldPort() + " ...");
            snifferWorld(worldPatches);
        } catch (Exception e) {
            log.error("[World]", e);
        }
    }

    public void snifferWorld(@Nullable Consumer<PatchRegistry> registerPatchsHook) throws Exception {
        final PatchRegistry patchRegistry = new PatchRegistry();
        if (registerPatchsHook != null) registerPatchsHook.accept(patchRegistry);
        simpleServer(configuration.getRemoteWorldAddress(), configuration.getRemoteWorldPort(), configuration.getLocalWorldPort(), configuration.isSslWorld(), null,
            (ch) -> {
                var res = new HexDumpProxyBackendHandlerWorld(ch, getLogManager());
                res.setPatchRegistry(patchRegistry);
                return res;
            }, endWorld :: set, f -> f.setPatchRegistry(patchRegistry));
    }

    public void snifferAuth(@Nullable Consumer<PatchRegistry> registerPatchsHook) throws Exception {
        final PatchRegistry patchRegistry = new PatchRegistry();
        if (registerPatchsHook != null) registerPatchsHook.accept(patchRegistry);
        simpleServer(configuration.getRemoteAuthAddress(), configuration.getRemoteAuthPort(), configuration.getLocalAuthPort(), configuration.isSslAuth(), null,
            (ch) -> {
                var res = new HexDumpProxyBackendHandlerWorld(ch, getLogManager());
                res.setPatchRegistry(patchRegistry);
                return res;
            }, endAuth :: set, f -> f.setPatchRegistry(patchRegistry));
    }

    protected void simpleServer(String remoteHost, int remotePort, int localPort, boolean ssl, String loggerName,
                                       Function<Channel, ChannelHandler> handler, Consumer<Runnable> stopFunction,
                                       @Nullable Consumer<HexDumpProxyFrontendHandler> frontendHook) throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        wakfuConnectionChannels.setBackProvider(ch);
                        if (ssl) {
                            try {
                                ch.pipeline().addLast(SSLContextHolder.forServer().newHandler(ch.alloc()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.exit(-2);
                            }
                        }
                        if (loggerName != null) {
                            ch.pipeline().addLast(new LoggingHandler(loggerName, LogLevel.INFO));
                        }
                        var res = new HexDumpProxyFrontendHandler(remoteHost, remotePort, () ->
                            new ChannelInitializer<>() {
                                @Override
                                protected void initChannel(SocketChannel ch2) throws Exception {
                                    if (ssl) {
                                        ch2.pipeline().addLast(SSLContextHolder.forClient().newHandler(ch2.alloc()));
                                    }
                                    if (loggerName != null) {
                                        ch2.pipeline().addLast(new LoggingHandler(loggerName + " back", LogLevel.INFO));
                                    }
                                    wakfuConnectionChannels.setFrontProvider(ch2);
                                    ch2.pipeline().addLast(handler.apply(ch));
                                }
                            },
                            getLogManager());
                        if (frontendHook != null) {
                            frontendHook.accept(res);
                        }
                        ch.pipeline().addLast(res);
                    }
                })
                .childOption(ChannelOption.AUTO_READ, false)
                .bind(localPort).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        stopFunction.accept(() -> {
            if (! bossGroup.isShutdown()) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
    }

    public IWakfulibLogger getLogManager() {
        if (logManager == null) throw new RuntimeException("LogManager is null");
        return logManager;
    }

    public void close() {
        log.info("Closing Sniffer...");
        try {
            endAuth.get().run();
        } catch (Exception ignored) {
        }
        try {
            if (endWorld.get() != null) {
                endWorld.get().run();
            }
        } catch (Exception ignored) {
        }
    }

    public interface SnifferStarter {
        @NonNull
        Triplet<Supplier<SnifferWindow>, Supplier<Sniffer>, BiConsumer<Sniffer, SnifferWindow>> init(@NonNull IConfiguration version);
    }
}
