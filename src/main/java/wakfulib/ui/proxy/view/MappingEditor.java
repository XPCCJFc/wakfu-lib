package wakfulib.ui.proxy.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import wakfulib.exception.NotImplementedException;
import wakfulib.ui.proxy.model.mapping.Mapping;
import wakfulib.ui.utils.IconUtils;
import wakfulib.ui.utils.ModificationTrakerCellRenderer;
import wakfulib.utils.data.ModifiableTuple;

public class MappingEditor extends JPanel {
    public Mapping model;
    private final JCustomTableModel tableModel;
    private final JTable table;

    public MappingEditor(Mapping model) {
        setLayout(new BorderLayout());

        tableModel = new JCustomTableModel(new MappingModel(model));
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ModificationTrakerCellRenderer());
        table.setRowSorter(new TableRowSorter<>(tableModel));
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel controlsPanel = new JPanel();
        final JButton addButton = new JButton("+");
        final ActionListener addRowAction = a -> {
            tableModel.addRow(new Vector<>(Arrays.asList(null, "")));
            selectRow(tableModel.getRowCount() - 1);
        };
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_N) {
                    addRowAction.actionPerformed(null);
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    tableModel.deleteRow(table.convertRowIndexToModel(table.getSelectedRow()));
                }
            }
        });
        addButton.addActionListener(addRowAction);
        controlsPanel.add(addButton);
        final JButton saveButton = new JButton();
        setIconOrText(saveButton, "/icons/menu-saveall_dark.png", "Save");
        saveButton.addActionListener(l -> {
            if (tableModel.hasAnyModification()) {
                tableModel.saveChanges(modifications -> {
                    for (ModifiableTuple<Integer, String> modification : modifications) {
                        model.put(modification._2, modification._1);
                    }
                });
            }
        });
        controlsPanel.add(saveButton);

        JButton favoriteButton = new JButton();
        setIconOrText(favoriteButton, "/icons/star.png", "Favorite");
        favoriteButton.setToolTipText("Set favorite for current version");
        controlsPanel.add(favoriteButton);
        favoriteButton.setEnabled(false);
        add(controlsPanel, BorderLayout.SOUTH);
    }

    private void setIconOrText(JButton button, String icon, String text) {
        var imageIcon = IconUtils.loadIcon(icon);
        if (imageIcon == null) {
            button.setText(text);
        } else {
            button.setIcon(imageIcon);
        }
    }

    public void editOpCode(int op) {
        final Integer boxedOp = op;
        for (int i = tableModel.getRowCount() - 1; i >= 0; --i) {
            if (boxedOp.equals(tableModel.getValueAt(i, 0))) {
                selectRow(i);
                return;
            }
        }
        tableModel.addRow(new Vector<>(Arrays.asList(op, "")));
        selectRow(tableModel.getRowCount() - 1);
    }

    private void selectRow(int i) {
        table.getSelectionModel().clearSelection();
        table.getSelectionModel().setSelectionInterval(i, i);
    }

    private static class MappingModel implements JCustomTableModel.Model {
        private final Mapping model;
        private final Vector<Class<?>> dataType;
        private final Vector<String> columnNames;
        private final Vector<Vector<Object>> values;

        public MappingModel(Mapping model) {
            this.model = model;
            this.dataType = new Vector<>(Arrays.asList(Integer.class, String.class));
            this.values = new Vector<>();
            model.forEach((message, opcode) -> values.add(new Vector<>(Arrays.asList(opcode, message))));
            this.columnNames = new Vector<>(Arrays.asList("OpCode" , "Message name"));
        }

        @Override
        public Vector<Vector<Object>> getValues() {
            return values;
        }

        @Override
        public Vector<Class<?>> getDataType() {
            return dataType;
        }

        @Override
        public Vector<String> getColumnNames() {
            return columnNames;
        }

        @Override
        public void saveChanges(Vector<Vector<Object>> modified) {
            System.out.println("saveChanges");
            throw new NotImplementedException();
        }

        @Override
        public boolean validateChange(Object newValue, Object oldValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                if (newValue instanceof String) {
                    try {
                        newValue = Integer.parseInt((String) newValue);
                    } catch (Exception e) {
                        return false;
                    }
                }
                if (newValue instanceof Integer) {
                    return (Integer) newValue > 0;
                }
            } else {
              return true;
            }
            return false;
        }
    }
}
