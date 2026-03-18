package wakfulib.ui.proxy.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wakfulib.internal.Version;

@AllArgsConstructor
@Getter
public enum Configuration implements IConfiguration {
    PANDORA("dispatch.platforms.wakfu.com", 5558, 5554, "pandora.platforms.wakfu.com", 5556, 5555, Version.v1_72_1),
    DATHURA("dispatch.platforms.wakfu.com", 5558, 5554, "dathura.platforms.wakfu.com", 5556, 5555, null),
    AERAFAL("dispatch.platforms.wakfu.com", 5558, 5554, "aerafal.platforms.wakfu.com", 5556, 5555, null),
    REMINGTON("dispatch.platforms.wakfu.com", 5558, 5554, "remington.platforms.wakfu.com", 5556, 5555, null),
    ELBOR("dispatch.platforms.wakfu.com", 5558, 5554, "elbor.platforms.wakfu.com", 5556, 5555, null),
    NOX("dispatch.platforms.wakfu.com", 5558, 5554, "nox.platforms.wakfu.com", 5556, 5555, null),
    PHAERIS("dispatch.platforms.wakfu.com", 5558, 5554, "phaeris.platforms.wakfu.com", 5556, 5555, null),
    EFRIM("dispatch.platforms.wakfu.com", 5558, 5554, "efrim.platforms.wakfu.com", 5556, 5555, null),
    DEV_TEST_AUTH_OFFI("dispatch.platforms.wakfu.com", 5558, 5554, "127.0.0.1", 5559, 5555, null),
    DEV_TEST("127.0.0.1", 5556, 5554, "127.0.0.1", 5558, 5555, Version.v1_68_0),
    YUYU("127.0.0.1", 5556, 5554, "127.0.0.1", 5558, 5555, Version.v1_63_0),
    DEV_ALPHA("127.0.0.1", 5556, 5554, "127.0.0.1", 5558, 5555, Version.v0_315, false, false, true),
    DEV_1_74_4("dispatch.platforms.wakfu.com", 5558, 5554, "pandora.platforms.wakfu.com", 5556, 5555, Version.v1_74_4, true, true, false),
    DEV_ARENA("127.0.0.1", 20556, 20555, "127.0.0.1", 20556, 20555, Version.v2_4_ARENA, false, false, true),
    DEV_ARENARETURNS("127.0.0.1", -1, -1, "minuit.arena-returns.com", 5555, 5555, Version.v2_70_ARENA, false, false, true),
    DEV_ARENARETURNS_LOCAL("127.0.0.1", -1, -1, "127.0.0.1", 5555, 5556, Version.v2_70_ARENA, false, false, true);

    private final String remoteAuthAddress;
    private final int remoteAuthPort;
    private final int localAuthPort;
    private final String remoteWorldAddress;
    private final int remoteWorldPort;
    private final int localWorldPort;
    private final Version version;
    private final boolean sslAuth;
    private final boolean sslWorld;
    private final boolean onlyWorld;

    Configuration(String remoteAuthAddress, int remoteAuthPort, int localAuthPort, String remoteWorldAddress, int remoteWorldPort,
                  int localWorldPort, Version version) {
        this(remoteAuthAddress, remoteAuthPort, localAuthPort, remoteWorldAddress, remoteWorldPort, localWorldPort, version, true, true, false);
    }
}
