package wakfulib.ui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
@AllArgsConstructor
public class RuntimeObject {
    private final Map<String, Object> fields = new HashMap<>();
    @Getter
    private final String name;


    public void addFields(@NonNull String name, @Nullable Object value) {
        fields.put(name, value);
    }

    public Set<Entry<String, Object>> getAllFields() {
        return fields.entrySet();
    }
}
