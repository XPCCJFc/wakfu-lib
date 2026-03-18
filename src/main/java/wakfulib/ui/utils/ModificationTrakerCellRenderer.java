package wakfulib.ui.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import wakfulib.ui.proxy.view.JCustomTableModel;

/**
 * Cellule destinée à reconnaitre si la valeur est en train d'être modifiée.
 * Affiche une bordure si c'est le cas.
 * Supporte les pipelines de rendu {@see CompositeTableCellRenderer}
 */
public class ModificationTrakerCellRenderer extends CompositeTableCellRenderer  {

    /**
     * La bordure à afficher en cas de modification non sauvegardée.
     */
    public ModifiedValueBorder modifiedValueBorder;

    /**
     * {@inheritDoc}
     */
    public ModificationTrakerCellRenderer() {
        this(null);
    }

    /**
     * {@inheritDoc}
     */
    public ModificationTrakerCellRenderer(TableCellRenderer renderAfterThis) {
        super(renderAfterThis);
        modifiedValueBorder = new ModifiedValueBorder(Color.red);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component res = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TableModel model = table.getModel();
        if (model instanceof JCustomTableModel && res instanceof JComponent) {
            JCustomTableModel myModel = (JCustomTableModel) model;
            JComponent resComp = ((JComponent) res);
            if (myModel.hasChanged(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(column))) {
//                resComp.setToolTipText(Translator.translate("valueModifiedToolTip"));
                resComp.setToolTipText("Value Modified");
                resComp.setBorder(modifiedValueBorder);
            } else {
                resComp.setToolTipText(null);
            }
        }
        return res;
    }
}
