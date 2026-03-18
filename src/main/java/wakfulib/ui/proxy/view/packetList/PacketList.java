package wakfulib.ui.proxy.view.packetList;

import static wakfulib.ui.proxy.SnifferWindow.OPTIONS;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.io.Format;
import wakfulib.ui.proxy.listeners.OptionListener;
import wakfulib.ui.proxy.model.FakePacket;
import wakfulib.ui.proxy.model.Packet;
import wakfulib.ui.proxy.model.PacketTableModel;
import wakfulib.ui.proxy.model.SSLPacketHS;
import wakfulib.ui.proxy.model.WakfuPacket;
import wakfulib.ui.proxy.model.mapping.FileMapping;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.ui.proxy.view.PacketListPopupMenu;

@Slf4j
public class PacketList extends JTable {

    private final PacketTableModel model;
    private final PacketListPopupMenu popupMenu;
    private final SnifferWindow snifferWindow;
    @Getter
    private int selectionStart = -1;
    @Getter
    private int selectionEnd = -1;

    public PacketList(SnifferWindow snifferWindow) {
        super(new PacketTableModel());
        this.snifferWindow = snifferWindow;
        setTableHeader(null);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model = (PacketTableModel) this.getModel();
        var settings = Settings.getInstance();
        setDefaultRenderer(Object.class, new PacketListCellRenderer(settings.getOptions()));

        OptionListener optionListener = c -> repaint();
        settings.registerForOptionChange("SERVER_BACKGROUND", optionListener);
        settings.registerForOptionChange("CLIENT_BACKGROUND", optionListener);
        settings.registerForOptionChange("FAKE_FOREGROUND", optionListener);
        settings.registerForOptionChange("SSL_BACKGROUND", optionListener);

        popupMenu = new PacketListPopupMenu(snifferWindow);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    var selectedRow = getSelectedRow();
                    if (selectedRow == -1) return;
                    var packet = model.get(convertRowIndexToModel(selectedRow));
                    snifferWindow.changePacketName(packet);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                this.mouseReleased(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        });
    }

    protected void showPopupMenu(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), snifferWindow);

        if (! getSelectionModel().isSelectionEmpty()) {
            final Object valueAt = model.getValueAt(convertRowIndexToModel(getSelectedRow()), 0);
            if (valueAt instanceof Packet) {
                popupMenu.setHasMapping(snifferWindow.getMapping() instanceof FileMapping);
                popupMenu.setSelectedMessage(convertRowIndexToModel(getSelectedRow()), ((Packet) valueAt));
                popupMenu.show(SwingUtilities.windowForComponent(this), pt.x, pt.y);
            }
        }
        e.consume();
    }

    public int incomingPacket(Packet packet) {
        return incomingPacket(packet, -1, OPTIONS.isALWAYS_BOTTOM_SCROLL());
    }

    public int incomingPacket(Packet packet, int at, boolean autoscroll) {
        model.add(packet, at);
        int row = at > -1 ? at : getRowCount() - 1;
        if (autoscroll) {
            scrollRectToVisible(getCellRect(row, getColumnCount(), true));
        }
        return row;
    }

    public void incomingPackets(List<? extends Packet> packets) {
        model.addBatch(packets);
        if (OPTIONS.isALWAYS_BOTTOM_SCROLL()) {
            scrollRectToVisible(getCellRect(getRowCount()-1, getColumnCount(), true));
        }
    }

    public void clear() {
        getSelectionModel().clearSelection();
        selectionEnd = -1;
        selectionStart = -1;
        model.clear();
    }

    public List<WakfuPacket> clearWithSave() {
        List<WakfuPacket> save = new ArrayList<>();
        Iterator<Packet> iterator = model.getIterator();
        while (iterator.hasNext()) {
            Packet next = iterator.next();
            if (next instanceof WakfuPacket) {
                save.add(((WakfuPacket) next));
            }
        }
        clear();
        return save;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void serialize(File selectedFile, Iterator<Packet> packetIterator) {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile)))) {
            List<Packet> packetView = new ArrayList<>();
            while (packetIterator.hasNext()) {
                packetView.add(packetIterator.next());
            }
            Format byName = Format.getByName(selectedFile.getAbsolutePath());
            if (byName != null) {
                byName.write(out, packetView);
            } else {
                log.warn("Unkown format for file: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("!", e);
        }
    }

    public void generateMapping(File selectedFile) {
        final HashMap<Integer, String> mapping = new HashMap<>();
        Iterator<Packet> iterator = model.getIterator();
        while (iterator.hasNext()) {
            final Object obj = iterator.next();
            if (! (obj instanceof WakfuPacket)) {
                continue;
            }
            WakfuPacket packet = (WakfuPacket) obj;
            final int opcode = packet.getOpcode();
            if (mapping.containsKey(opcode)) {
                continue;
            }
            String name;
            if (packet.getDef() != null) {
                name = packet.getDef().getName();
            } else {
                name = "";
            }
            mapping.put(opcode, name);
        }
        final HashMap<Integer, String> mappingSorted = mapping.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap ::new));

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(selectedFile)))) {
            mappingSorted.forEach((k, v) -> out.println(k + " = " + v));
            out.flush();
        } catch (Exception e) {
            log.error("!", e);
        }
    }

    public void removeSelected(int orElseIndex) {
        int end, start;
        if (selectionStart > selectionEnd) {
            end = selectionStart;
            start = selectionEnd;
        } else {
            end = selectionEnd;
            start = selectionStart;
        }

        selectionEnd = -1;
        selectionStart = -1;
        if (start > -1) {
            model.removeRange(start, end);
            getSelectionModel().clearSelection();
        } else if (orElseIndex > -1) {
            model.removeRange(orElseIndex, orElseIndex + 1);
            getSelectionModel().clearSelection();
        } else {
            var realSelectMin = getSelectionModel().getMinSelectionIndex();
            var realSelectMax = getSelectionModel().getMaxSelectionIndex();
            if (realSelectMin == -1 || realSelectMax == -1) return;
            model.removeRange(realSelectMin, realSelectMax);
        }

    }

    public void showSelection() {
        final int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            scrollRectToVisible(getCellRect(selectedRow - 1, getColumnCount(), true));
        }
    }

    public void beginSelect(int rowIndex) {
        selectionStart = rowIndex;
    }

    public void endSelect(int rowIndex) {
        selectionEnd = rowIndex;
    }

    @NonNull
    public Iterator<Packet> modelIterator() {
        return model.getIterator();
    }

    @NonNull
    public Iterator<Packet> selectionIterator(int beginIndex, int endIndex) {
        return model.getIterator(beginIndex, endIndex);
    }

    @NonNull
    public Iterator<Packet> selectionIterator() {
        int selectionEnd = this.selectionEnd;
        int selectionStart = this.selectionStart;
        
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }

        return model.getIterator(selectionStart, selectionEnd);
    }

    public int insertBlankPacket(int at, boolean autoscroll) {
        return incomingPacket(new FakePacket(false), at, autoscroll);
    }

    private class PacketListCellRenderer extends DefaultTableCellRenderer {

        private final Options options;

        public PacketListCellRenderer(Options options) {
            this.options = options;
            setOpaque(true);
            setFont(getFont());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof WakfuPacket) {
                WakfuPacket wakfuPacket = (WakfuPacket) value;
                if (wakfuPacket.isFromServer()) {
                    setBackground(options.getSERVER_BACKGROUND());
                } else {
                    setBackground(options.getCLIENT_BACKGROUND());
                }

                if (wakfuPacket.isFake()) {
                    setForeground(options.getFAKE_FOREGROUND());
                } else {
                    setForeground(Color.BLACK);
                }
            } else if (value instanceof SSLPacketHS) {
                setBackground(options.getSSL_BACKGROUND());
            } else if (value instanceof FakePacket) {
                setBackground(options.getCUSTOM_BACKGROUND());
            }
            if (selectionEnd != -1 && selectionStart != -1) {
                if (row < selectionEnd && row > selectionStart || row < selectionStart && row > selectionEnd) {
                    setBackground(getBackground().darker());
                }
            }
            if (row == selectionEnd || row == selectionStart) {
                Color background = getBackground();
                setBackground(new Color(background.getRed(), 255, background.getBlue(), background.getAlpha()));
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
