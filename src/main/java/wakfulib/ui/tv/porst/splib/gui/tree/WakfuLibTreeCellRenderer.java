package wakfulib.ui.tv.porst.splib.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import wakfulib.ui.tv.porst.splib.gui.GuiHelpers;
import wakfulib.ui.tv.porst.splib.gui.tree.IconTree.CustomTreeCellRenderer;
import wakfulib.ui.utils.MutliLineString;

public class WakfuLibTreeCellRenderer extends CustomTreeCellRenderer {

    final JTextArea cache = new JTextArea() {{
        setOpaque(false);
        setFont(new Font(GuiHelpers.getMonospaceFont(), Font.PLAIN, 15));
    }
    };

    @Override
    public Color getBackground() {
        return null;
    }

    @Override
    public Color getBackgroundNonSelectionColor() {
        return null;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MutliLineString) {
                cache.setText(((MutliLineString) userObject).getText());
                return cache;
            }
        }
        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
