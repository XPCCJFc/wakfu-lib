package test.perf;

import test.message.toClient.BigPacketToClientMessage;
import test.message.toServer.BigPacketToServerMessage;
import wakfulib.WakfuLib;
import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.impl.WakfulibLogAdapter;
import wakfulib.logic.Session;
import wakfulib.logic.SessionMultiplexer;
import wakfulib.logic.WakfuServerConnection;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.event.annotation.EventHandler;
import wakfulib.logic.event.def.ConnectionClosedEvent;
import wakfulib.logic.event.def.ConnectionEstablishedEvent;

public final class TestServerLoad {
    static BigPacketToClientMessage out;

    static final World all = new World(WakfulibLogAdapter.getLogger(World.class, "-World"));

    public static void main(String[] args) throws Exception {
        var wakfuLib = new WakfuLib();
        wakfuLib.init(Version.TEST);
        VersionRegistry.scan("test.message");
        out = VersionRegistry.messageInstance(BigPacketToClientMessage.class);
        new WakfuServerConnection.WakfuServerConnectionBuilder(
            new EventManager.EventManagerBuilder()
                .register(new TestServer())
            .build())
            .bind(9999)
            .start();
    }

    private static class World extends SessionMultiplexer {
        World(IWakfulibLogger logger) {
            super(logger);
            setTrackFailure(true);
        }
    }

    private static class TestServer {

        @EventHandler
        public void onConnect(Session s, ConnectionEstablishedEvent e) {
            all.add(s);
        }

        @EventHandler
        public void onMessage(Session s, BigPacketToServerMessage bigPacketMessage) {
            all.broadcast(out);
        }

        @EventHandler
        public void onDisc(Session s, ConnectionClosedEvent e) {
            all.remove(s);
        }
    }
}
