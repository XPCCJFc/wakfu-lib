package wakfulib.ui.proxy.view.packetview.listeners;

import java.util.function.Predicate;
import javax.swing.JComponent;
import wakfulib.ui.proxy.model.DataPacket;

public class ConditionalJComponentEnablingViewListener extends JComponentEnablingViewListener {

    private final Predicate<DataPacket> condition;

    public ConditionalJComponentEnablingViewListener(JComponent component, Predicate<DataPacket> condition) {
        super(component);
        this.condition = condition;
    }

    @Override
    public void onPacketArrival(DataPacket packet) {
        if (condition.test(packet)) {
            super.onPacketArrival(packet);
        }
    }
}
