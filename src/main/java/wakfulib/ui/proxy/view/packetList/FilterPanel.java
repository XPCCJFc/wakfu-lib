package wakfulib.ui.proxy.view.packetList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import wakfulib.ui.proxy.model.WakfuPacket;
import wakfulib.ui.utils.RegexFilter;

public class FilterPanel extends JPanel {

    private final JCheckBox fromServerCheckBox;
    private final JCheckBox fromClientCheckBox;
    private final JCheckBox sslCheckBox;
    private final JCheckBox unknownPacketCheckBox;
    private final JTextField messageName;
    private final DefaultListModel<Integer> hideListModel;
    public FilterPanel(TableRowSorter<TableModel> sorter, PacketList packetList) {
        setLayout(new BorderLayout());
        fromServerCheckBox = new JCheckBox("From server");
        fromServerCheckBox.setSelected(true);
        fromServerCheckBox.addItemListener(m -> sorter.allRowsChanged());
        fromClientCheckBox = new JCheckBox("From client");
        fromClientCheckBox.addItemListener(m -> sorter.allRowsChanged());
        fromClientCheckBox.setSelected(true);
        sslCheckBox = new JCheckBox("ssl");
        sslCheckBox.addItemListener(m -> sorter.allRowsChanged());
        sslCheckBox.setSelected(true);
        unknownPacketCheckBox = new JCheckBox("Unknown");
        unknownPacketCheckBox.addItemListener(m -> sorter.allRowsChanged());
        unknownPacketCheckBox.setSelected(false);
        ArrayList<RowFilter<TableModel, Integer>> filters = new ArrayList<>();
        RowFilter<TableModel, Integer> fromServerFilter = new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            TableModel model = entry.getModel();
                final Object valueAt = model.getValueAt(entry.getIdentifier(), 0);
                if (valueAt instanceof WakfuPacket) {
                    WakfuPacket value = (WakfuPacket) valueAt;
                    if (hideListModel.contains(value.getOpcode())) {
                        return false;
                    }
                    if (unknownPacketCheckBox.isSelected()) {
                        if (value.isFake() || value.isKnown()) {
                            return false;
                        }
                    }
                    if (value.isFromServer()) {
                        return fromServerCheckBox.isSelected();
                    } else {
                        return fromClientCheckBox.isSelected();
                    }
                } else {
                    return sslCheckBox.isSelected();
                }
            }
        };
        sorter.setSortsOnUpdates(true);
        filters.add(fromServerFilter);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(fromServerCheckBox);
        leftPanel.add(fromClientCheckBox);
        leftPanel.add(unknownPacketCheckBox);
        leftPanel.add(sslCheckBox);
        messageName = new JTextField();
        messageName.setToolTipText("Filter packets by name");
        RegexFilter filter = new RegexFilter(0);
        messageName.getDocument().addDocumentListener(new AllUpdateDocumentListener(() -> {
            filter.updatePattern(messageName.getText());
            sorter.allRowsChanged();
        }));
        messageName.setAlignmentX(Component.CENTER_ALIGNMENT);
        filters.add(filter);
        leftPanel.add(messageName);
        add(leftPanel, BorderLayout.WEST);
        hideListModel = new DefaultListModel<>();
        JList<Integer> filteredList = new JList<>(hideListModel);
        JScrollPane listInScrollPane = new JScrollPane(filteredList);
        listInScrollPane.setToolTipText("Hidden packets");
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(listInScrollPane, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

//        JButton insertPacket = new JButton("Insert");
//        insertPacket.setEnabled(false);
//        insertPacket.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                var minSelectionIndex = packetList.getSelectionModel().getMinSelectionIndex();
//                int i = packetList.insertBlankPacket(minSelectionIndex, false);
//                packetList.setRowSelectionInterval(i, i);
//            }
//        });
//        buttons.add(insertPacket);

//        JButton delete = new JButton("Delete");
//        delete.setEnabled(false);
//        filteredList.addListSelectionListener(e -> {
//            System.out.println(e.getFirstIndex() + " | " + e.getLastIndex());
//        });
//        delete.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int index = filteredList.getSelectedIndex();
//                if (index != -1) {
//                    hideListModel.removeElementAt(index);
//                    sorter.allRowsChanged();
//                }
//            }
//        });
//        buttons.add(delete);

        rightPanel.add(buttons, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.CENTER);
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    public void hidePacketOfType(int i) {
        hideListModel.addElement(i);
    }
}
