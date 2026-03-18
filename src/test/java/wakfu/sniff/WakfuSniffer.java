package wakfu.sniff;

import lombok.extern.slf4j.Slf4j;
import wakfu.sniff.logic.BasicWorldConnector;
import wakfu.sniff.protocol.ClientProxiesResultMessage;
import wakfu.sniff.protocol.path.ProxyPatch;
import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.logic.proxy.Sniffer;
import wakfulib.logic.proxy.patch.PatchRegistry;
import wakfulib.ui.proxy.SnifferLauncher;
import wakfulib.ui.proxy.conf.IConfiguration;
import wakfulib.utils.data.Triplet;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.function.Consumer;

public class WakfuSniffer extends Sniffer {
//
//    public WakfuSniffer() {
//        ClientProxiesResultMessage clientProxiesResultMessage;
//        try {
//            clientProxiesResultMessage = VersionRegistry.get(ClientProxiesResultMessage.class);
//        } catch (Exception e) {
//            clientProxiesResultMessage = null;
//        }
//        if ((clientProxiesResultMessage == null || clientProxiesResultMessage.getOpCode() == - 1 && ! ONLY_WORLD)) {
//            LOGGER.info("[Auth] Proxying 127.0.0.1:" + LOCAL_PORT_AUTH + " to " + REMOTE_HOST_AUTH + ':' + REMOTE_PORT_AUTH + " ...");
//            LOGGER.warn("[Auth] ClientProxiesResultMessage opcode is unknown, starting without world handler");
//            LOGGER.info("[Auth] Sniffer auth starting");
//            try {
//                simpleServer(REMOTE_HOST_AUTH, REMOTE_PORT_AUTH, LOCAL_PORT_AUTH, SSL_AUTH, "Auth",
//                    ch -> {
//                        WAKFU_CONNECTION_CHANNELS.setBackProvider(ch);
//                        return new HexDumpProxyBackendHandlerWorld(ch, getLogManager());
//                    },
//                    endAuth :: set);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            if (! ONLY_WORLD) {
//                LOGGER.info("[Auth] Proxying 127.0.0.1:" + LOCAL_PORT_AUTH + " to " + REMOTE_HOST_AUTH + ':' + REMOTE_PORT_AUTH + " ...");
//                PatchRegistery.register((short) clientProxiesResultMessage.getOpCode(), new ProxyPatch(REMOTE_HOST_AUTH, "127.0.0.1", LOCAL_PORT_WORLD));
//            }
//            LOGGER.info("[World] Proxying 127.0.0.1:" + LOCAL_PORT_WORLD + " to " + REMOTE_HOST_WORLD + ':' + REMOTE_PORT_WORLD + " ...");
//            try {
//                if (! ONLY_WORLD) {
//                    new Thread(() -> {
//                        try {
//                            LOGGER.info("[Auth] Sniffer auth starting");
//                            snifferAuth(SSL_AUTH);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }).start();
//                }
//                LOGGER.info("[Auth] Sniffer world starting");
//                snifferWorld(SSL_WORLD);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static final IConfiguration WAKFU_LOCAL_CONF = IConfiguration.customNoAuth("Wakfu local",
            "127.0.0.1", BasicWorldConnector.PORT, BasicWorldConnector.PROX_PORT, Version.v1_68_0, false
    );

    public static final IConfiguration WAKFU_SNIF_CONF = IConfiguration.custom("Wakfu sniff",
            "wakfu-dispatcher.ankama-games.com", 5558, BasicWorldConnector.PROX_PORT,
            "SET ME LATER", -1, 5558, Version.v1_91_2, true, true
    );

    public static void main(String[] args) {
        if (false && verifyWakfu()) {
            JOptionPane.showMessageDialog(null, "Veuillez fermer Wakfu avant de lancer le sniffer.", "Veuillez fermer Wakfu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SnifferLauncher.DEFAULT_CONF = WAKFU_SNIF_CONF;
        SnifferLauncher.DEFAULT_CONFS = Collections.singletonList(WAKFU_SNIF_CONF);
        SnifferLauncher.DEFAULT_VERSION = WAKFU_SNIF_CONF.getVersion();
        Sniffer.launchSnifferWithEditableConfiguration(getStater(true));
    }

    public static SnifferStarter getStater(boolean sniffing) {
        return (conf) -> {
            return new Triplet<>(() -> WakfuSnifferWindow.launch(conf.getVersion(), sniffing),
                    () -> new WakfuSniffer(),
                    (sniffer, win) -> {

                        Consumer<PatchRegistry> patchRegistryConsumer = null;
                        try {
                            var clientProxiesResultMessage = VersionRegistry.get(ClientProxiesResultMessage.class);
                            if ((clientProxiesResultMessage == null || clientProxiesResultMessage.getOpCode() == -1 && !conf.isOnlyWorld())) {
                                logManager.warn("[Auth] ClientProxiesResultMessage opcode is unknown, starting without world handler");
                            } else {
                                patchRegistryConsumer = p -> p.register((short) clientProxiesResultMessage.getOpCode(),
                                        new ProxyPatch(clientProxiesResultMessage, conf.getLocalWorldPort()));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        sniffer.start(win, patchRegistryConsumer, null);
                    });
        };
    }

    @Slf4j
    public static class WakfuSnifferDirect {

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> Sniffer.launchSniffer(WAKFU_LOCAL_CONF, getStater(true)));
        }
    }

    protected static boolean verifyWakfu() {
        try {
            Process process = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe /v /fi \"WINDOWTITLE eq WAKFU\" /NH /FO CSV");
            BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if ((line = processReader.readLine()) != null) {
                return line.startsWith("\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
