package wakfulib.ui.proxy.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.model.Nameable;
import wakfulib.ui.proxy.model.Packet;
import wakfulib.ui.proxy.model.WakfuPacket;

public class PacketListPopupMenu extends JPopupMenu {

    private final AtomicReference<Packet> packet;
    private final AtomicInteger rowIndex;
    private final JPacketMenuItem modifyName;
    private final JPacketMenuItem endSelect;
    private final List<JPacketMenuItem> allMenus;

    public PacketListPopupMenu(SnifferWindow snifferWindow) {
        allMenus = new ArrayList<>();
        packet = new AtomicReference<>();
        rowIndex = new AtomicInteger();
        modifyName = new JPacketMenuItem("Modify name", p -> p instanceof Nameable);
        modifyName.addActionListener(l -> snifferWindow.changePacketName(this.packet.get()));
        add(modifyName);
        JPacketMenuItem hide = new JPacketMenuItem("Hide", p -> p instanceof WakfuPacket);
        hide.addActionListener(l -> snifferWindow.addToHideFilter(((WakfuPacket) packet.get()).getOpcode()));
        add(hide);
        JPacketMenuItem beginSelect = new JPacketMenuItem("Begin selection", p -> true);
        endSelect = new JPacketMenuItem("End selection", p -> true);
        beginSelect.addActionListener(l -> {
            snifferWindow.beginSelect(rowIndex.get());
            endSelect.setEnabled(true);
        });
        endSelect.setEnabled(false);
        endSelect.addActionListener(l -> snifferWindow.endSelect(rowIndex.get()));
        add(beginSelect);
        add(endSelect);
        JPacketMenuItem delete = new JPacketMenuItem("Delete", p -> true);
        delete.addActionListener(l -> {
            setVisible(false);
            snifferWindow.removeSelectedAction(rowIndex.get());
        });
        add(delete);
        allMenus.addAll(Arrays.asList(modifyName, delete, beginSelect, endSelect, hide));
    }

    public void selectionCleared() {
        endSelect.setEnabled(false);
    }

    public void setSelectedMessage(int rowIndex, Packet packet) {
        this.packet.set(packet);
        this.rowIndex.set(rowIndex);
        allMenus.forEach(m -> m.setVisible(m.accept(packet)));
    }

    public void setHasMapping(boolean hasMapping) {
        modifyName.setVisible(hasMapping);
    }

    private static class JPacketMenuItem extends JMenuItem {
        private final Predicate<Packet> acceptPredicate;

        public JPacketMenuItem(String text, Predicate<Packet> acceptPredicate) {
            super(text);
            this.acceptPredicate = acceptPredicate;
        }

        public boolean accept(Packet packet) {
            return acceptPredicate.test(packet);
        }
    }
}
