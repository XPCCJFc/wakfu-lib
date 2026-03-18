package wakfulib.logic.proxy.patch;

import java.util.HashMap;
import java.util.Map;

public class PatchRegistry {
    public Map<Short, Patch> patches = new HashMap<>();
    
    public void register(short opCode, Patch patch) {
        patches.put(opCode, patch);
    }

    public Patch getPatchForPacket(short opCode) {
        return patches.get(opCode);
    }
}
