package wakfulib.ui.proxy.view.packetview.listeners;

import wakfulib.ui.proxy.model.DataPacket;

public interface PacketViewListener {
    void onPacketArrival(DataPacket packet);
    void onPacketRemoval();
}
