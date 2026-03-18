package wakfulib.ui.proxy.view.packetview.listeners;

import javax.swing.JComponent;
import lombok.AllArgsConstructor;
import wakfulib.ui.proxy.model.DataPacket;

@AllArgsConstructor
public class JComponentEnablingViewListener implements PacketViewListener {

    private final JComponent component;

    @Override
    public void onPacketArrival(DataPacket packet) {
        component.setEnabled(true);
    }

    @Override
    public void onPacketRemoval() {
        component.setEnabled(false);
    }
}
