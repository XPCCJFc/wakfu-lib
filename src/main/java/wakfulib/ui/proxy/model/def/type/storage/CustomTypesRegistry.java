package wakfulib.ui.proxy.model.def.type.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfulib.ui.proxy.model.def.type.Type;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomTypesRegistry {

    private static final Map<String, Type> CUSTOM_TYPES = new HashMap<>();

    public static void register(Type type, String label) {
        log.info("Custom type '{}' registered with label '{}'.", type.getName(), label);
        CUSTOM_TYPES.put(label, type);
    }

    public static Iterable<Entry<String, Type>> getAllCustomTypesWithLabel() {
        return CUSTOM_TYPES.entrySet();
    }
}
