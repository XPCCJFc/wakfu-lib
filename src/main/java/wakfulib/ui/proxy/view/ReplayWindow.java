package wakfulib.ui.proxy.view;

import static wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener.PACKET_DEFINITION_HIGHLIGHT_LEVEL;

import io.netty.buffer.Unpooled;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import wakfulib.logic.OutPacket;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.model.FakePacket;
import wakfulib.ui.proxy.view.packetview.impl.ExternalPacketViewButtonFactory;
import wakfulib.ui.tv.porst.jhexview.JHexView;
import wakfulib.ui.tv.porst.jhexview.SimpleDataProvider;

public class ReplayWindow extends JFrame {

    public ReplayWindow(DataPacket packet, SnifferWindow snifferWindow) {
        super("Replay");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(500, 500));
        setLayout(new BorderLayout());
        JHexView view = new JHexView();
        view.setSelectionColor(new Color(182, 218, 255));
        view.setFontColorAsciiView(Color.black);
        add(view, BorderLayout.CENTER);

        view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
        view.setData(new SimpleDataProvider(packet.getData()));
        view.setDefinitionStatus(JHexView.DefinitionStatus.DEFINED);
        view.setEnabled(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(ExternalPacketViewButtonFactory.gotoButton(view));
        buttonPanel.add(ExternalPacketViewButtonFactory.offsetButton(view));
        JButton send = new JButton("Send");
        send.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                snifferWindow.send(Unpooled.wrappedBuffer(view.getData().getData()), packet.isFromServer());
            }
        });
        buttonPanel.add(send);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReplayWindow(new FakePacket(OutPacket.simpleBuffer().writeIntStringUTF8("COUCOU").toByteArray(), false), null).setVisible(true));
    }
}
