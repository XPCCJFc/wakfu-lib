package wakfulib.logic.proxy.patch;

import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logic.OutPacket;

import java.nio.ByteBuffer;

public interface Patch {
    @Nullable
    default OutPacket patchToServer(byte archTarget, short opCode, byte[] rawData) {
        return patch(new OutPacket(archTarget, opCode), ByteBuffer.wrap(rawData));
    }

    @Nullable
    default OutPacket patchToClient(short opCode, byte[] rawData) {
        return patch(new OutPacket(false, opCode), ByteBuffer.wrap(rawData));
    }

    @Nullable
    OutPacket patch(@NonNull OutPacket outPacket, @NonNull ByteBuffer data);
}
