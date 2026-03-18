package wakfulib.ui.proxy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import wakfulib.doc.NonNull;

public class PacketTableModel extends AbstractTableModel {

    private final List<Packet> model;

    private static final String COLUMN_NAME = "Packet";

    public PacketTableModel() {
        model = new ArrayList<>();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAME;
    }

    @Override
    public int getRowCount() {
        return model.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Packet.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return model.get(rowIndex);
    }

    public void clear() {
        int size = model.size();
        if (size == 0) return;
        model.clear();
        fireTableRowsDeleted(0, size - 1);
    }

    public Iterator<Packet> getIterator() {
        return model.iterator();
    }

    public void removeRange(int beginIndex, int endIndex) {
        if (beginIndex == endIndex) {
            endIndex = endIndex + 1;
        }
        model.subList(beginIndex, endIndex).clear();
        fireTableRowsDeleted(beginIndex, endIndex - 1);
    }

    @NonNull
    public Iterator<Packet> getIterator(int beginIndex, int endIndex) {
        return new Iterator<>() {
            private int traveled = beginIndex;

            @Override
            public boolean hasNext() {
                return traveled <= endIndex;
            }

            @Override
            public Packet next() {
                return model.get(traveled++);
            }
        };
    }
    
    public Packet get(int at) {
        return model.get(at);
    }

    public void add(Packet packet) {
        int at = model.size();
        model.add(packet);
        fireTableRowsInserted(at, at);
    }

    public void add(Packet packet, int at) {
        if (at == -1) at = model.size();
        model.add(at, packet);
        fireTableRowsInserted(at, at);
    }

    public void addBatch(List<? extends Packet> packet) {
        if (packet.size() == 0) return;
        int beforeSize = model.size();
        model.addAll(packet);
        fireTableRowsInserted(beforeSize, model.size() - 1);
    }
}
