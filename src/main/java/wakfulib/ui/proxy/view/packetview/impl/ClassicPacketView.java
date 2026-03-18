package wakfulib.ui.proxy.view.packetview.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.view.packetList.PacketInterpreter;
import wakfulib.ui.proxy.view.packetview.PacketView;

public class ClassicPacketView extends JPanel implements PacketView {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private final JTextArea packetDataDisplay;
    private final PacketInterpreter packetInterpreter;
    private DataPacket selectedValue;

    public ClassicPacketView() {
        packetDataDisplay = new JTextArea();
        packetInterpreter = new PacketInterpreter(packetDataDisplay.getHighlighter());
        packetDataDisplay.setEditable(false);
        packetDataDisplay.setFont(new Font("monospaced", Font.PLAIN, 15));
        setLayout(new BorderLayout());
        add(packetInterpreter, BorderLayout.SOUTH);
        add(new JScrollPane(packetDataDisplay, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    public void select(DataPacket selectedValue) {
        this.selectedValue = selectedValue;
        byte[] bytes = selectedValue.getData();
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if ((j + 1) % 16 == 0) {
                hexChars[j * 3 + 2] = '\n';
            } else {
                hexChars[j * 3 + 2] = ' ';
            }
        }
        String rep = new String(hexChars);
        packetDataDisplay.setText(rep);
        packetInterpreter.setPacket(selectedValue);
    }

    public void clear() {
        packetInterpreter.clear();
        packetDataDisplay.setText("");
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public DataPacket getSelectedValue() {
        return selectedValue;
    }
}
