package wakfulib.ui.proxy.model.mapping;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Mapping {
    void save();

    void reload();

    String getByOp(int opcode);

    Integer getByName(String simpleName);

    void put(String simpleName, int opCode);

    Stream<Map.Entry<String, Integer>> stream();

    boolean containsOp(int opcode);

    void clear();

    void forEach(BiConsumer<String, Integer> action);
}
