package test.perf;

import lombok.extern.slf4j.Slf4j;
import test.message.toClient.BigPacketToClientMessage;
import test.message.toServer.BigPacketToServerMessage;
import wakfulib.WakfuLib;
import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.logic.Session;
import wakfulib.logic.WakfuClientConnection.WakfuClientConnectionBuilder;
import wakfulib.logic.event.EventManager;
import wakfulib.logic.event.annotation.EventHandler;
import wakfulib.logic.event.def.ConnectionClosedEvent;
import wakfulib.logic.event.def.ConnectionEstablishedEvent;

@Slf4j
public class TestClient {
    static BigPacketToServerMessage out;

    public static void main(String[] args)throws Exception {
        var wakfuLib = new WakfuLib();
        wakfuLib.init(Version.TEST);
        VersionRegistry.scan("test.message");
        out = VersionRegistry.messageInstance(BigPacketToServerMessage.class);
        EventManager.EventManagerBuilder eventManager = new EventManager.EventManagerBuilder();
        eventManager.register(new TestClient());

        new WakfuClientConnectionBuilder(eventManager.build())
            .bind("localhost", 9999)
            .start();
    }
    
    @EventHandler
    public void onEvent(Session s, ConnectionEstablishedEvent e) {
        s.send(out);
    }
    
    @EventHandler
    public void onPacket(Session s, BigPacketToClientMessage e) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        s.send(out);
    }
    
    @EventHandler
    public void onDisc(Session s, ConnectionClosedEvent e) {
        log.info("Disconnected");
    }

}
