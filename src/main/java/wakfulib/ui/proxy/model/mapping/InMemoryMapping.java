package wakfulib.ui.proxy.model.mapping;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class InMemoryMapping implements Mapping {
    private final Map<String, Integer> opcodeByPacketName;
    private final Map<Integer, String> packetNameByOpcode;

    public InMemoryMapping() {
        opcodeByPacketName = new java.util.HashMap<>();
        packetNameByOpcode = new java.util.HashMap<>();
    }

    @Override
    public void save() {

    }

    @Override
    public void reload() {

    }

    @Override
    public String getByOp(int opcode) {
        return packetNameByOpcode.get(opcode);
    }

    @Override
    public Integer getByName(String simpleName) {
        return opcodeByPacketName.get(simpleName);
    }

    @Override
    public void put(String simpleName, int opCode) {
        opcodeByPacketName.put(simpleName, opCode);
        packetNameByOpcode.put(opCode, simpleName);
    }

    @Override
    public Stream<Map.Entry<String, Integer>> stream() {
        return opcodeByPacketName.entrySet().stream();
    }

    @Override
    public boolean containsOp(int opcode) {
        return packetNameByOpcode.containsKey(opcode);
    }

    @Override
    public void clear() {
        packetNameByOpcode.clear();
        opcodeByPacketName.clear();
    }

    @Override
    public void forEach(BiConsumer<String, Integer> action) {
        opcodeByPacketName.forEach(action);
    }
}
