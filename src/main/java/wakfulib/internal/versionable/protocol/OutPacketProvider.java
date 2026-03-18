package wakfulib.internal.versionable.protocol;

import wakfulib.doc.NonNull;
import wakfulib.logic.OutPacket;

public interface OutPacketProvider {
    @NonNull
    OutPacket getOutPacket();
}
