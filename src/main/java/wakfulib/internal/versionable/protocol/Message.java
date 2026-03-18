package wakfulib.internal.versionable.protocol;

import wakfulib.internal.Internal;
import wakfulib.internal.versionable.ProtocolMessage;
import wakfulib.logic.event.Event;

/**
 * Represent a message that may be sent to a Wakfu Client or Server
 */
public abstract class Message<T extends Message> implements Event, OutPacketProvider, ProtocolMessage<T> {

    public static final int UNKNOWN_OPCODE = -1;

    @Internal
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private int opCode = UNKNOWN_OPCODE;

    /**
     * @return the opcode of the packet
     */
    public final int getOpCode() {
        return opCode;
    }

    @Deprecated
    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }
}
