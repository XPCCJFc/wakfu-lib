package wakfulib.ui.proxy.view.packetList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.utils.ExceptionDialog;

@Slf4j
public class PacketInterpreter extends JPanel {

    private final Highlighter highlighter;
    private int offset = 0;

    private static final Highlighter.HighlightPainter PAINTER = new DefaultHighlighter.DefaultHighlightPainter(Color.orange);

    public PacketInterpreter(Highlighter highlighter) {
        this.highlighter = highlighter;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void setPacket(DataPacket p) {
        try {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(p.getData());
            removeAll();
            offset = 0;
            highlighter.removeAllHighlights();
            addLabel("Size: " + byteBuf.readShort(), 2);
            if (! p.isFromServer()) {
                addLabel("ArchTarget: " + byteBuf.readByte(), 1);
            }
            addLabel("OpCode: " + byteBuf.readShort(), 2);
            revalidate();
            repaint();
        } catch (Exception e) {
            log.error("Error", e);
            new ExceptionDialog("Error", e).setVisible(this);
        }
    }

    protected void addLabel(String label, int byteSize) {
        JLabel jLabel = new JLabel(label);
        final int offsetT = offset;
        offset = offset + byteSize * 3;
        add(jLabel);
        jLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    highlighter.addHighlight(offsetT, byteSize * 3 + offsetT - 1, PAINTER);
                } catch (Exception ex) {
                    log.error("Highlighter error ", ex);
                    new ExceptionDialog("Error", ex).setVisible(PacketInterpreter.this);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                highlighter.removeAllHighlights();
            }
        });
    }

    public void clear() {
        highlighter.removeAllHighlights();
        removeAll();
        revalidate();
        repaint();
    }
}
