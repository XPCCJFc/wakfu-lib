package wakfulib.ui.utils;

import java.util.EnumMap;
import java.util.Map;
import javax.swing.JRadioButtonMenuItem;

public class RadioButtonGroupEnumAdapter<E extends Enum<E>> {
    final private Map<E, JRadioButtonMenuItem> buttonMap;

    public RadioButtonGroupEnumAdapter(Class<E> enumClass) {
        this.buttonMap = new EnumMap<>(enumClass);
    }

    public void importMap(Map<E, JRadioButtonMenuItem> map) {
        for (E e : map.keySet()) {
            this.buttonMap.put(e, map.get(e));
        }
    }

    public void associate(E e, JRadioButtonMenuItem btn) {
        this.buttonMap.put(e, btn);
    }

    public E getValue() {
        for (E e : this.buttonMap.keySet()) {
            JRadioButtonMenuItem btn = this.buttonMap.get(e);
            if (btn.isSelected()) {
                return e;
            }
        }
        return null;
    }

    public void setValue(E e) {
        JRadioButtonMenuItem btn = (e == null) ? null : this.buttonMap.get(e);
        if (btn == null) {
            // the following doesn't seem efficient...
            // but since when do we have more than say 10 radiobuttons?
            for (JRadioButtonMenuItem b : this.buttonMap.values()) {
                b.setSelected(false);
            }

        } else {
            btn.setSelected(true);
        }
    }

    public JRadioButtonMenuItem getForVersion(E version) {
        return buttonMap.get(version);
    }
}
