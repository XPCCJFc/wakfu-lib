package wakfulib.internal.versionable.protocol;

import java.nio.ByteBuffer;
import wakfulib.doc.NonNull;
import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;

public abstract class ToClientMessage<T extends ToClientMessage> extends Message<T> {
    @Override
    public T unserialize(@NonNull ByteBuffer buffer) {
        throw new NotImplementedException("This ToClientMessage cannot be unserialized");
    }

    @Override
    @NonNull
    public OutPacket encode() {
        throw new NotImplementedException("This ToClientMessage cannot be encoded");
    }

    @Override
    @NonNull
    public OutPacket getOutPacket() {
        return new OutPacket(false, getOpCode());
    }
}
