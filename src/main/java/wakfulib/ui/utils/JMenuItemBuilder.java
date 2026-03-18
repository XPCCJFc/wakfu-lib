package wakfulib.ui.utils;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class JMenuItemBuilder {

    private final JMenuItem menuItem;

    public JMenuItemBuilder(String label, final Consumer<ActionEvent> onClick) {
        menuItem = new JMenuItem(label);
        menuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClick.accept(e);
            }
        });
    }

    public JMenuItemBuilder withKeyStroke(KeyStroke keyStroke) {
        menuItem.setAccelerator(keyStroke);
        return this;
    }

    public JMenuItemBuilder withIcon(Icon icon) {
        menuItem.setIcon(icon);
        return this;
    }

    public JMenuItemBuilder withIcon(String icon) {
        withIcon(IconUtils.loadIcon(icon));
        return this;
    }

    public JMenuItem build() {
        return menuItem;
    }
}
