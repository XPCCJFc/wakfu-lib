package wakfulib.logic.proxy.patch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wakfulib.logic.OutPacket;

import java.nio.ByteBuffer;

public final class DiscardPatch implements Patch {
    @Nullable
    @Override
    public OutPacket patch(@NotNull OutPacket outPacket, @NotNull ByteBuffer data) {
        return null;
    }
}
