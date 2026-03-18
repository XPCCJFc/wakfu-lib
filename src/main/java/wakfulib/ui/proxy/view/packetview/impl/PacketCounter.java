package wakfulib.ui.proxy.view.packetview.impl;

import javax.swing.JLabel;
import wakfulib.ui.utils.Memory;

public class PacketCounter extends JLabel {

    private int counter;

    public PacketCounter() {
        this.counter = 0;
    }

    public void increment() {
        counter++;
        update();
    }

    public void increment(int value) {
        counter = counter + value;
        update();
    }

    public void reset() {
        counter = 0;
        update();
    }

    private void update() {
        setText(counter + " packets " + Memory.getPercentageUsedFormatted());
    }
}
