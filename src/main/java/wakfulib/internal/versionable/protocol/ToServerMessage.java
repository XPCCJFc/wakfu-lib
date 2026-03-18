package wakfulib.internal.versionable.protocol;

import java.nio.ByteBuffer;
import lombok.Setter;
import wakfulib.doc.NonNull;
import wakfulib.internal.Internal;
import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;

public abstract class ToServerMessage<T extends ToServerMessage> extends Message<T> {

    public static final byte UNKNOWN_ARCH_TARGET = -1;

    @Internal @Setter(onMethod = @__(@Deprecated))
    public byte archTarget = UNKNOWN_ARCH_TARGET;

    @Override
    public T unserialize(@NonNull ByteBuffer buffer) {
        throw new NotImplementedException(this.getClass().getSimpleName() + ".unserialize()");
    }

    @Override
    public @NonNull OutPacket getOutPacket() {
        return new OutPacket(archTarget, getOpCode());
    }
}
