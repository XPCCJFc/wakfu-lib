package wakfulib.internal.versionable.scripted;

import org.jetbrains.annotations.NotNull;
import wakfulib.internal.versionable.ProtocolMessage;
import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;

public abstract class AbstractProtocolScriptFactory implements ProtocolMessage<ProtocolScriptInstance> {
    @Override
    public final @NotNull OutPacket encode() {
        throw new NotImplementedException();
    }
}
