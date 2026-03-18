package wakfulib.ui.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;

public class RegexFilter extends RowFilter<TableModel, Integer> {

    public static final String EMPTY_STRING = "";

    /**
     * Symbole pour indiquer que l'expression régulière ne doit pas prendre en compte la casse
     */
    private static final String CASE_INSENSITIVE_FLAG = "(?i)";
    private Matcher matcher;
    private int columnTargeted;

    public RegexFilter(int columnTargeted) {
        this.columnTargeted = columnTargeted;
        compilePattern(EMPTY_STRING);
    }

    private void compilePattern(String regex) {
        try {
            matcher = Pattern.compile(CASE_INSENSITIVE_FLAG + regex).matcher(EMPTY_STRING);
        } catch (PatternSyntaxException e) {
            matcher = null;
        }
    }

    public void updatePattern(String regex) {
        compilePattern(regex);
    }

    public boolean include(RowFilter.Entry<? extends TableModel,? extends Integer> entry) {
        if (matcher == null) return true;
        TableModel model = entry.getModel();
        Object value = model.getValueAt(entry.getIdentifier(), columnTargeted);
        if (value == null) {
            return true;
        }
        matcher.reset(value.toString());
        return matcher.find();
    }
}
