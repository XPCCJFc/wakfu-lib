package wakfulib.ui.proxy.conf;

import wakfulib.internal.Version;

public interface IConfiguration {
    String getRemoteAuthAddress();

    int getRemoteAuthPort();

    int getLocalAuthPort();

    String getRemoteWorldAddress();

    int getRemoteWorldPort();

    int getLocalWorldPort();

    wakfulib.internal.Version getVersion();

    boolean isSslAuth();

    boolean isSslWorld();

    boolean isOnlyWorld();

    String name();

    static IConfiguration custom(String name, String remoteAuthAddress, int remoteAuthPort, int localAuthPort,
                                        String remoteWorldAddress, int remoteWorldPort, int localWorldPort,
                                         Version version, boolean sslAuth, boolean sslWorld) {
        return new IConfiguration() {
            @Override
            public String getRemoteAuthAddress() {
                return remoteAuthAddress;
            }

            @Override
            public int getRemoteAuthPort() {
                return remoteAuthPort;
            }

            @Override
            public int getLocalAuthPort() {
                return localAuthPort;
            }

            @Override
            public String getRemoteWorldAddress() {
                return remoteWorldAddress;
            }

            @Override
            public int getRemoteWorldPort() {
                return remoteWorldPort;
            }

            @Override
            public int getLocalWorldPort() {
                return localWorldPort;
            }

            @Override
            public Version getVersion() {
                return version;
            }

            @Override
            public boolean isSslAuth() {
                return sslAuth;
            }

            @Override
            public boolean isSslWorld() {
                return sslWorld;
            }

            @Override
            public boolean isOnlyWorld() {
                return false;
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    static IConfiguration customNoAuth(String name, String remoteWorldAddress, int remoteWorldPort, int localWorldPort,
                                              Version version, boolean sslWorld) {
        return new IConfiguration() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String getRemoteAuthAddress() {
                return "";
            }

            @Override
            public int getRemoteAuthPort() {
                return -1;
            }

            @Override
            public int getLocalAuthPort() {
                return -1;
            }

            @Override
            public String getRemoteWorldAddress() {
                return remoteWorldAddress;
            }

            @Override
            public int getRemoteWorldPort() {
                return remoteWorldPort;
            }

            @Override
            public int getLocalWorldPort() {
                return localWorldPort;
            }

            @Override
            public Version getVersion() {
                return version;
            }

            @Override
            public boolean isSslAuth() {
                return false;
            }

            @Override
            public boolean isSslWorld() {
                return sslWorld;
            }

            @Override
            public boolean isOnlyWorld() {
                return true;
            }
        };
    }


}
