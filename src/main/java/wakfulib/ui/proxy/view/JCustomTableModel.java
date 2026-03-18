package wakfulib.ui.proxy.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import wakfulib.utils.data.ModifiableTuple;

public class JCustomTableModel extends AbstractTableModel {

    /**
     * Le nom affichée de chaque colonne de la table.
     */
    private final Vector<String> titles;
    /**
     * La liste des valeurs de chaque lignes.
     */
    private final Vector<Vector<Object>> values;
    /**
     * Le buffer de modifications.
     */
    private final HashMap<ID, ModificationValueBuffer> modification;
    /**
     * Le type de données que contiendra chaque colonne de la table.
     */
    private final Vector<Class<?>> dataType;
    private final int nbHiddenColumn;
    private final boolean isEditable;
    private final Model model;

    public JCustomTableModel(Model model) {
        this(model, 0, true);
    }

    public JCustomTableModel(Model model, int nbHiddenColumn) {
        this(model, nbHiddenColumn, true);
    }

    public JCustomTableModel(Model model, int nbHiddenColumn, boolean isEditable) {
        this.model = model;
        this.titles = model.getColumnNames();
        this.values = model.getValues();
        this.modification = new HashMap<>();
        this.dataType = model.getDataType();

        this.nbHiddenColumn = nbHiddenColumn;
        this.isEditable = isEditable;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return titles.size() + nbHiddenColumn;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (titles.size() <= columnIndex) return "";
        return titles.get(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values.get(rowIndex).get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < dataType.size()) {
            return dataType.get(columnIndex);
        }
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable;
    }

    /**
     * Modifie la valeur situé la ligne <i>rowIndex</i> et la colonne <i>columnIndex</i> par la valeur aValue et ajoute la modification au buffer de modifications.
     * @param aValue La nouvelle valeur.
     * @param rowIndex L'indice de la ligne modifiée.
     * @param columnIndex L'indice de la colonne modifiée.
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (model.validateChange(aValue, values.get(rowIndex).get(columnIndex), rowIndex, columnIndex)) {
            addModificationToBuffer(aValue, rowIndex, columnIndex);
            values.get(rowIndex).set(columnIndex, aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * Ajoute une modification au buffer de modification en vérifiant que la case n'a pas déjà été modifiée.
     * Si oui, alors met la valeur à jour, si non, l'ajoute simplement au buffer.
     * @param aValue La nouvelle valeur.
     * @param rowIndex L'indice de la ligne modifiée.
     * @param columnIndex L'indice de la colonne modifiée.
     */
    private void addModificationToBuffer(Object aValue, int rowIndex, int columnIndex) {
        ID id = new ID(rowIndex, columnIndex, getColumnCount());
        ModificationValueBuffer modificationValue = modification.get(id);
        if (modificationValue == null) {
            modificationValue = new ModificationValueBuffer(getValueAt(rowIndex, columnIndex));
            modification.put(id, modificationValue);
        }
        modificationValue.setNewValue(aValue);
    }

    public boolean hasChanged(int row, int column) {
        ID id = new ID(row, column, getColumnCount());
        if (! modification.containsKey(id)) return false;
        return modification.get(id).hasChanged();
    }

    /**
     * Applique au modèle les modifications contenues dans le buffer, puis le vide.
     */
    public void saveChanges(Consumer<Collection<ModifiableTuple<Integer, String>>> modifiedValueConsumer) {
        modification.forEach((id, modificationBuffer) ->
            id.getRowAndColumn((row, column) -> values.get(row).set(column, modificationBuffer.newValue)));

        Map<Integer, ModifiableTuple<Integer, String>> res = new HashMap<>();

        for (Map.Entry<ID, ModificationValueBuffer> entry : modification.entrySet()) {
            final ModificationValueBuffer value = entry.getValue();
            final ID key = entry.getKey();
            ModifiableTuple<Integer, String> row = res.get(key.getRow());
            if (row == null) {
                res.put(key.getRow(), row = new ModifiableTuple<>());
            }
            if (key.getColumn() == 0) {
                row._1 = (Integer) value.newValue;
                row._2 = (String) getValueAt(key.getRow(), 1);
            } else if (key.getColumn() == 1) {
                row._1 = (Integer) getValueAt(key.getRow(), 0);
                row._2 = (String) value.newValue;
            }
        }

        modifiedValueConsumer.accept(res.values());

        modification.clear();
        fireTableDataChanged();
    }

    public boolean hasAnyModification() {
        if (modification.isEmpty()) return false;
        return modification.entrySet().stream().anyMatch(entry -> entry.getValue().hasChanged());
    }

    /**
     * Classe permettant de faciliter la manipulation du buffer de modification.
     * Contient deux attributs : une ancienne valeur à laquelle peut être associée une nouvelle valeur.
     */
    private static class ModificationValueBuffer {
        private final Object oldValue;
        private Object newValue;

        public ModificationValueBuffer(Object oldValue) {
            this.oldValue = oldValue;
        }

        public void setNewValue(Object newValue) {
            this.newValue = newValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public boolean hasChanged() {
            return ! Objects.equals(oldValue, newValue);
        }
    }

    /**
     * Ajoute une ligne à la fin de la table.
     * @param data Le jeu de données à ajouter à la table.
     */
    public void addRow(Vector<Object> data) {
        int lastPosition = getRowCount();
        values.insertElementAt(data, lastPosition);
        fireTableRowsInserted(lastPosition, lastPosition);
    }

    /**
     * Supprime une ligne de la table.
     * Supprime aussi toute valeurs dans le buffer de modifications qui appartenait à l'entrée supprimée.
     * @param selectedRow La ligne à supprimer.
     */
    public void deleteRow(int selectedRow) {
        List<ID> modificationToBuffer = new ArrayList<>();
        int columnCount = getColumnCount();
        int minRange = selectedRow * columnCount;
        int maxRange = minRange + columnCount - 1;
        for (Map.Entry<ID, ModificationValueBuffer> entry : modification.entrySet()) {
            int key = entry.getKey().getId();
            if (key >= minRange && key <= maxRange) {
                modificationToBuffer.add(entry.getKey());
            }
        }
        for (ID integer : modificationToBuffer) {
            modification.remove(integer);
        }
        List<Map.Entry<ID, ModificationValueBuffer>> collect =
            modification.entrySet().
                stream().
                filter((entry) -> entry.getKey().id > maxRange).
                sorted(Comparator.comparingInt(entry -> entry.getKey().id)).
                collect(Collectors.toList());

        for (Map.Entry<ID, ModificationValueBuffer> idModificationValueBufferEntry : collect) {
            ID id = idModificationValueBufferEntry.getKey();
            modification.put(new ID(id.id - columnCount, getColumnCount()), modification.get(id));
            modification.remove(id);
        }

        values.removeElementAt(selectedRow);
        fireTableRowsDeleted(selectedRow, selectedRow);
    }

    public Vector<Vector<Object>> getValues() {
        return values;
    }

    /**
     * Classe permettant de faciliter la manipulation du buffer de modifications.
     * Permet d'assigner à n'importe quelle cellule de la table un identifiant unique en fonction de sa ligne et de sa colonne.
     */
    private static class ID {
        int id;
        int columnCount;

        public ID(int id, int columnCount) {
            this.id = id;
            this.columnCount = columnCount;
        }

        public ID(int row, int column, int columnCount) {
            this.columnCount = columnCount;
            id = row * columnCount + column;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ID id1 = (ID) o;
            return id == id1.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        public int getRow() {
            return id / columnCount;
        }

        public int getColumn() {
            return id % columnCount;
        }

        public void getRowAndColumn(BiConsumer<Integer, Integer> callback) {
            callback.accept(getRow(), getColumn());
        }
    }

    /**
     * Un modèle selon le modèle MVC
     */
    public interface Model {
        /**
         * Méthode permettant de récuperer sous forme brut les données qu'il contient
         */
        Vector<Vector<Object>> getValues();

        /**
         * Retourne un vecteur réprésentant le type des données stockés, colonne par colonnes
         */
        Vector<Class<?>> getDataType();

        Vector<String> getColumnNames();

        /**
         * Sauvegarde les changements apportés au modèle
         */
        void saveChanges(Vector<Vector<Object>> modified);

        /**
         * Méthode permettant de valider ou non un changement d'une donnée.
         * Si le changement n'est pas accepté, alors cette méthode doit se charger
         * d'avertir l'utilisateur de la cause de l'échec de la modification.
         *
         * @param newValue l'ancienne valeur de la donnée
         * @param oldValue la nouvelle valeur de la donnée
         * @param rowIndex l'indice (du modèle) de la ligne où la donnée à été changée
         * @param columnIndex l'indice (du modèle) de la colonne où la donnée à été changée
         * @return Vrai si le changement est accepté, Faux sinon.
         */
        boolean validateChange(Object newValue, Object oldValue, int rowIndex, int columnIndex);
    }
}
