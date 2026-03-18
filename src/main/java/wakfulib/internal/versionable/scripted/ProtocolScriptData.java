package wakfulib.internal.versionable.scripted;

import lombok.Data;
import wakfulib.internal.VersionRange;
import wakfulib.ui.proxy.model.def.PacketDefinition;

@Data
public class ProtocolScriptData {
    private int opcode;
    private VersionRange range;
    private PacketDefinition def;
}
