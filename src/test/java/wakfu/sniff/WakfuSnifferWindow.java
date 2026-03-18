package wakfu.sniff;

import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.ui.proxy.SnifferOptions;
import wakfulib.ui.proxy.SnifferWindow;

import javax.swing.*;

public class WakfuSnifferWindow {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> launch(WakfuSniffer.WAKFU_SNIF_CONF.getVersion(), false));
    }

    public static SnifferWindow launch(Version defaultVersion, boolean sniffing) {
        Version.setCurrent(defaultVersion);
        VersionRegistry.scan("wakfu");
        SnifferWindow.preventExit = false;
        SnifferWindow.setNoAuthInitializer(() -> {
            System.out.println("Well this is awkward...");
        });
        return new SnifferWindow(defaultVersion, SnifferOptions.builder()
                .hideAuth(false)
                .packetListEnabled(true)
                .sniffing(sniffing)
                .build());
    }
}
