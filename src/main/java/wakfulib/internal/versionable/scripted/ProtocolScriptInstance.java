package wakfulib.internal.versionable.scripted;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;

public class ProtocolScriptInstance extends AbstractProtocolScriptInstance {

    private final Map<String, Object> data;

    public ProtocolScriptInstance(AbstractProtocolScriptFactory factory) {
        super(factory);
        data = new HashMap<>();
    }

    @Override
    public @NotNull OutPacket encode() {
        //TODO faire QQ chose
        throw new NotImplementedException();
    }

    @Override
    public final ProtocolScriptInstance unserialize(@NotNull ByteBuffer buffer) {
        //TODO faire QQ chose
        throw new NotImplementedException();
    }
}
