package wakfulib.ui.proxy.view.packetview.impl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import wakfulib.ui.tv.porst.jhexview.JHexView;

public class ExternalPacketViewButtonFactory {

    public static JButton gotoButton(JHexView view) {
        JButton goTo = new JButton("Goto");
        goTo.setToolTipText("Go to the provided offset in the packet view");
        goTo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String offsetStr = JOptionPane.showInputDialog(view, "Input byte number");
                try {
                    var offset = Integer.parseInt(offsetStr);
                    if (offset < 0) offset = 0;
                    view.setSelectionLength(0);
                    view.setCurrentOffset(offset);
                    view.requestFocus();
                } catch (Exception ignored) {
                    
                }
            }
        });
        return goTo;
    }

    public static JButton offsetButton(JHexView view) {
        JButton goPlus = new JButton("Offset");
        goPlus.setToolTipText("Add the provided offset to the selection in the packet view");
        goPlus.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String offsetStr = JOptionPane.showInputDialog(view, "Input byte number");
                if (offsetStr == null || ! offsetStr.isEmpty()) return;
                try {
                    view.setCurrentOffset(view.getCurrentOffset() + Integer.parseInt(offsetStr));
                    view.requestFocus();
                } catch (Exception ignored) {
                    
                }
            }
        });
        return goPlus;
    }

}
