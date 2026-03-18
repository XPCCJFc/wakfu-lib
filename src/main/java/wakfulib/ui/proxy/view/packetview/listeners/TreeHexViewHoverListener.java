package wakfulib.ui.proxy.view.packetview.listeners;

import java.awt.Color;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import lombok.RequiredArgsConstructor;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.tv.porst.jhexview.IHexViewHoverListener;
import wakfulib.ui.tv.porst.jhexview.JHexView;
import wakfulib.ui.utils.RGB;

@RequiredArgsConstructor
public class TreeHexViewHoverListener implements IHexViewHoverListener {

    public static final Color BG_HOVER_PACKET_DEF = RGB.newColorWithAlpha(Color.RED, 0.3);
    public static final Color FG_HOVER_PACKET_DEF = Color.BLACK;
    public static final int PACKET_DEFINITION_HIGHLIGHT_LEVEL = 0;

    private final JTree tree;
    private final JHexView view;
    private Object lastSelected;

    @Override
    public void hoverChanged(int nibble) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration children = node.children();
        int currentN = 0;
        while (children.hasMoreElements()) {
            Object o = children.nextElement();
            if (! (o instanceof PacketDefinitionNode)) return;
            PacketDefinitionNode child = (PacketDefinitionNode) o;
            int sizeInByte = child.getSize();
            int sizeInChar = sizeInByte * 2;
            currentN = currentN + sizeInChar;
            if (nibble < currentN) {//TODO optimisation Btree search with offset
                tree.setSelectionPath(new TreePath(child.getPath()));//plutot selectionner avec les lignes
                if (lastSelected != child) {
                    view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
                    view.colorize(PACKET_DEFINITION_HIGHLIGHT_LEVEL, ((currentN / 2) - sizeInByte), sizeInByte, FG_HOVER_PACKET_DEF, BG_HOVER_PACKET_DEF);
                    lastSelected = child;
                }
                return;
            }
        }
        view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
        lastSelected = null;
        tree.clearSelection();
    }

    @Override
    public void hoverRemoved() {
        tree.clearSelection();
    }
}
