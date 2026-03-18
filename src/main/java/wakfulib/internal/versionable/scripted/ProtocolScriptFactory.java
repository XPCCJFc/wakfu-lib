package wakfulib.internal.versionable.scripted;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import wakfulib.ui.proxy.model.def.PacketDefinition;

public class ProtocolScriptFactory extends AbstractProtocolScriptFactory {

    private final ProtocolScriptData data;

    public ProtocolScriptFactory(ProtocolScriptData data) {
        this.data = data;
    }

    @Override
    public int getOpCode() {
        return data.getOpcode();
    }

    @Override
    public ProtocolScriptInstance unserialize(byte[] data) {
        return unserialize(ByteBuffer.wrap(data));
    }

    @Override
    public ProtocolScriptInstance unserialize(@NotNull ByteBuffer buffer) {
        ProtocolScriptInstance protocolScriptInstance = new ProtocolScriptInstance(this);
        protocolScriptInstance.unserialize(buffer);
        return protocolScriptInstance;
    }

    @Override
    public PacketDefinition def() {
        return data.getDef();
    }
}
