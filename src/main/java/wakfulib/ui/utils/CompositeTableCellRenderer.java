package wakfulib.ui.utils;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Classe permettant d'afficher une cellule permettant de former une pipeline de rendu
 */
public abstract class CompositeTableCellRenderer extends DefaultTableCellRenderer {

    private final TableCellRenderer otherRenderer;

    /**
     * Creer un nouveau afficheur
     * @param other l'afficheur à utiliser après
     */
    protected CompositeTableCellRenderer(TableCellRenderer other) {
        otherRenderer = other;
    }

    /**
     * {@inheritDoc}
     *
     * Par défaut utilise le rendu fournis par {@see DefaultTableCellRenderer}
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (otherRenderer == null) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            return otherRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
