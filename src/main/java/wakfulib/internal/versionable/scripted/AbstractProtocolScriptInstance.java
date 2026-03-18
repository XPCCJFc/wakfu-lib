package wakfulib.internal.versionable.scripted;

import wakfulib.internal.versionable.ProtocolMessage;
import wakfulib.ui.proxy.model.def.PacketDefinition;

public abstract class AbstractProtocolScriptInstance implements ProtocolMessage<ProtocolScriptInstance> {

    protected final AbstractProtocolScriptFactory factory;

    public AbstractProtocolScriptInstance(AbstractProtocolScriptFactory factory) {
        this.factory = factory;
    }

    @Override
    public final PacketDefinition def() {
        return factory.def();
    }

    @Override
    public int getOpCode() {
        return factory.getOpCode();
    }
}
