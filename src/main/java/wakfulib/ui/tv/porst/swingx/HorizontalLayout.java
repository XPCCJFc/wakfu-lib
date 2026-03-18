package wakfulib.ui.tv.porst.swingx;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import wakfulib.ui.tv.porst.swingx.util.Separator;


public class HorizontalLayout extends AbstractLayoutManager {
    private static final long serialVersionUID = 8640046926840737487L;

    private int gap;

    public HorizontalLayout() {
        this(0);
    }

    //TODO should we allow negative gaps?
    public HorizontalLayout(int gap) {
        this.gap = gap;
    }

    public int getGap() {
        return gap;
    }

    //TODO should we allow negative gaps?
    public void setGap(int gap) {
        this.gap = gap;
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        Dimension size = parent.getSize();

        int height = size.height - insets.top - insets.bottom;
        int width = insets.left;

        for (int i = 0, c = parent.getComponentCount(); i < c; i++) {
            Component m = parent.getComponent(i);

            if (m.isVisible()) {
                m.setBounds(width, insets.top, m.getPreferredSize().width, height);
                width += m.getSize().width + gap;
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension pref = new Dimension(0, 0);
        Separator<Integer> sep = new Separator<Integer>(0, gap);

        for (int i = 0, c = parent.getComponentCount(); i < c; i++) {
            Component m = parent.getComponent(i);
            if (m.isVisible()) {
                Dimension componentPreferredSize =
                    parent.getComponent(i).getPreferredSize();
                pref.height = Math.max(pref.height, componentPreferredSize.height);
                pref.width += componentPreferredSize.width + sep.get();
            }
        }

        Insets insets = parent.getInsets();
        pref.width += insets.left + insets.right;
        pref.height += insets.top + insets.bottom;

        return pref;
    }
}
