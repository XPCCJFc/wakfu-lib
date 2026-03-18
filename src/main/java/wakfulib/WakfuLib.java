package wakfulib;

import wakfulib.doc.NonNull;
import wakfulib.internal.Version;
import wakfulib.internal.resources.ResourceManager;
import wakfulib.internal.resources.Translator;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logic.AuthHandler;
import wakfulib.logic.WakfuClientConnection.WakfuClientConnectionBuilder;
import wakfulib.logic.WakfuServerConnection.WakfuServerConnectionBuilder;
import wakfulib.logic.event.EventManager;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The main entry point for the WakfuLib framework.
 * Use this class to initialize the library with a specific game version
 * and to setup client or server connections.
 */
public class WakfuLib {

    /**
     * Creates a new instance of WakfuLib using the default resources folder.
     * The default folder is located at {@code %LOCALAPPDATA%\Ankama\zaap\wakfu\contents}.
     */
    public WakfuLib() {
        ResourceManager.setResourcesFolder(new File(System.getenv("LOCALAPPDATA") + "\\Ankama\\zaap\\wakfu\\contents"));
        Translator.setInstance();
    }

    /**
     * Creates a new instance of WakfuLib using a custom resources folder.
     *
     * @param ressourceFolder The folder containing Wakfu game resources.
     */
    public WakfuLib(File ressourceFolder) {
        ResourceManager.setResourcesFolder(ressourceFolder);
        Translator.setInstance();
    }

    /**
     * Creates a new instance of WakfuLib using a custom resources folder and a custom translator path.
     *
     * @param ressourceFolder The folder containing Wakfu game resources.
     * @param translatorPath The path to the custom translator file.
     */
    public WakfuLib(File ressourceFolder, String translatorPath) {
        ResourceManager.setResourcesFolder(ressourceFolder);
        Translator.setInstance(translatorPath);
    }

    /**
     * Initializes the library for a specific game version.
     * This must be called before performing any network operations that depend on protocol versions.
     *
     * @param version The game version to use.
     */
    public void init(Version version) {
        if (Version.getCurrent() != version) {
            Version.setCurrent(version);
        }
    }

    /**
     * Starts a fake client connection that handles authentication before connecting to the world server.
     *
     * @param logManager The logger to use for connection events.
     * @param sslWorld Whether to use SSL for the world server connection.
     * @param address The authentication server address.
     * @param port The authentication server port.
     * @param ssl Whether to use SSL for the authentication server connection.
     * @param authHandler The handler to process authentication logic and results.
     * @param worldHandlerProducerWithToken A function that produces a world server handler given the authentication token and an event manager builder.
     * @throws Exception if any error occurs during connection setup.
     */
    public static void fakeClientWithAuth(IWakfulibLogger logManager, boolean sslWorld, String address, int port, boolean ssl, AuthHandler authHandler, BiFunction<String, EventManager.EventManagerBuilder, Object> worldHandlerProducerWithToken) throws Exception {
        EventManager.EventManagerBuilder eventManager = new EventManager.EventManagerBuilder();
        eventManager.register(authHandler);
        new WakfuClientConnectionBuilder(eventManager.build())
            .bind(address, port)
            .withSSL(ssl)
            .withLogger((c) -> logManager)
            .start();
        if (authHandler.getResultCode() == -1) {
            logManager.error("AuthHandler resultCode = " + authHandler.getResultCode());
            return;
        }
        logManager.info("Selected wakfulib.logic.proxy = " + authHandler.getSelectedAddress() + ":" + authHandler.getSelectedPort());
        EventManager.EventManagerBuilder eventManagerForGame = new EventManager.EventManagerBuilder();
        eventManagerForGame.register(worldHandlerProducerWithToken.apply(authHandler.getToken(), eventManagerForGame));
        new WakfuClientConnectionBuilder(eventManagerForGame.build())
            .bind(authHandler.getSelectedAddress(), authHandler.getSelectedPort())
            .withSSL(sslWorld)
            .withLogger((c) -> logManager)
            .start();
    }

    /**
     * Starts a fake client connection that handles authentication before connecting to the world server.
     *
     * @param logManager The logger to use for connection events.
     * @param sslWorld Whether to use SSL for the world server connection.
     * @param address The authentication server address.
     * @param port The authentication server port.
     * @param ssl Whether to use SSL for the authentication server connection.
     * @param authHandler The handler to process authentication logic and results.
     * @param worldHandlerProducerWithToken A function that produces a world server handler given the authentication token.
     * @throws Exception if any error occurs during connection setup.
     */
    public void fakeClientWithAuth(IWakfulibLogger logManager, boolean sslWorld, String address, int port, boolean ssl, AuthHandler authHandler, Function<String, Object> worldHandlerProducerWithToken) throws Exception {
        fakeClientWithAuth(logManager, sslWorld, address, port, ssl, authHandler, (s, e) -> worldHandlerProducerWithToken.apply(s));
    }

    /**
     * Starts a server listener on the specified port.
     *
     * @param port The port to listen on.
     * @param serverHandler The handler for incoming server connections.
     * @param ssl Whether to use SSL for incoming connections.
     * @throws Exception if any error occurs during server setup.
     */
    public static void server(int port, @NonNull Object serverHandler, boolean ssl) throws Exception {
        EventManager.EventManagerBuilder eventManager = new EventManager.EventManagerBuilder();
        eventManager.register(serverHandler);
        var builder = new WakfuServerConnectionBuilder(eventManager.build());
        if (ssl) {
            builder.withSSL();
        }
        builder.bind(port)
            .start();
    }
}
