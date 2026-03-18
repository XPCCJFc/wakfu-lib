package wakfulib.ui.tv.porst.splib.gui.tree;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import wakfulib.ui.utils.IconUtils;


/**
 * Represents a tree where the nodes are custom icons.
 */
public class IconTree extends JTree {

	/**
	 * Creates a new icon tree object.
	 *
	 * @param model The model of the icon tree.
	 */
	public IconTree(TreeModel model) {
		super(model);

		setCellRenderer(new CustomTreeCellRenderer());
	}
    /**
     * Renderer used to display icons in tree nodes.
     */
    public static class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

        CustomTreeCellRenderer() {
            UIManager.put("Tree.collapsedIcon", COLLAPSE_ICON);
            UIManager.put("Tree.expandedIcon", EXPAND_ICON);
        }
        @Override
        public Component getTreeCellRendererComponent(JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            Icon icon = null;
            if (value instanceof IconNode) {
                icon = ((IconNode) value).getIcon();
            }
            if (icon == null) {
                if (leaf && row != 0 ) {
                    icon = getLeafIcon();
                } else if (row == 0) {
                    if (leaf) {
                        icon = EXPAND_EMPTY_ICON;
                    } else if (expanded) {
                        icon = getOpenIcon();
                    } else {
                        icon = getClosedIcon();
                    }
                }
            }
            setIcon(icon);

            //we can not call super.getTreeCellRendererComponent method, since it overrides our setIcon call and cause rendering of labels to '...' when node expansion is done
            //so, we copy (and modify logic a little bit) from super class method:

            final String stringValue = tree.convertValueToText(value, sel,
                expanded, leaf, row, hasFocus);

            this.hasFocus = hasFocus;
            setText(stringValue);
            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            setEnabled(tree.isEnabled());
            setComponentOrientation(tree.getComponentOrientation());
            selected = sel;
            return this;
        }

        @Override
        public Icon getDefaultClosedIcon() {
            return getClosedIcon();
        }

        @Override
        public Icon getDefaultLeafIcon() {
            return getLeafIcon();
        }

        @Override
        public Icon getDefaultOpenIcon() {
            return getOpenIcon();
        }

        @Override
        public Icon getLeafIcon() {
            return FIELD_ICON;
        }

        @Override
        public Icon getOpenIcon() {
            return COLLAPSE_ICON;
        }

        @Override
        public Icon getClosedIcon() {
            return EXPAND_ICON;
        }

        public static final Icon FIELD_ICON;
        public static final Icon EXPAND_ICON;
        public static final Icon COLLAPSE_ICON;
        public static final Icon EXPAND_EMPTY_ICON;

        static {
            FIELD_ICON = IconUtils.loadIcon("/icons/field.png");
            EXPAND_EMPTY_ICON = IconUtils.loadIcon("/icons/hide.png");
            BufferedImage image = IconUtils.loadImage("/icons/arrowCollapse.png");
            if (image == null) {
                image = new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
            }
            EXPAND_ICON = IconUtils.antiAliased(IconUtils.rotate(image, 180.0d));
            COLLAPSE_ICON = IconUtils.antiAliased(IconUtils.rotate(image, -90.0d));
        }
    }
}
