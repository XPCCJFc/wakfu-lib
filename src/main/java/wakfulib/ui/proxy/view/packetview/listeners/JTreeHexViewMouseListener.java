package wakfulib.ui.proxy.view.packetview.listeners;

import static wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener.BG_HOVER_PACKET_DEF;
import static wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener.FG_HOVER_PACKET_DEF;
import static wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener.PACKET_DEFINITION_HIGHLIGHT_LEVEL;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import lombok.AllArgsConstructor;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.tv.porst.jhexview.JHexView;

@AllArgsConstructor
public class JTreeHexViewMouseListener extends MouseAdapter implements TreeSelectionListener {

    private final JTree tree;
    private final JHexView view;

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
        if (path != null) {
            Object lastPathComponent = path.getLastPathComponent();
            if (lastPathComponent instanceof PacketDefinitionNode) {
                PacketDefinitionNode node = (PacketDefinitionNode) lastPathComponent;
                if (SwingUtilities.isRightMouseButton(e)) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Objects.toString(node.getValue())), null);
                }
                if (node.getSize() == 0) return;
                view.colorize(PACKET_DEFINITION_HIGHLIGHT_LEVEL, node.getOffset(), node.getSize(), FG_HOVER_PACKET_DEF, BG_HOVER_PACKET_DEF);
            }
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
        if (path != null) {
            Object lastPathComponent = path.getLastPathComponent();
            if (lastPathComponent instanceof PacketDefinitionNode) {
                PacketDefinitionNode node = (PacketDefinitionNode) lastPathComponent;
                if (node.getSize() == 0) return;
                view.colorize(PACKET_DEFINITION_HIGHLIGHT_LEVEL, node.getOffset(), node.getSize(), FG_HOVER_PACKET_DEF, BG_HOVER_PACKET_DEF);
            }
        }
    }
}
