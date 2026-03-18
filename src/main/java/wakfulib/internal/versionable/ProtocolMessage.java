package wakfulib.internal.versionable;

import wakfulib.beans.structure.SerializerIn;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logic.OutPacket;
import wakfulib.logic.event.Event;
import wakfulib.ui.proxy.model.def.PacketDefinition;

/**
 * Interface representing a protocol message in the Wakfu network communication.
 * Protocol messages can be both sent (encoded) and received (deserialized).
 *
 * @param <T> the type of the protocol message
 */
public interface ProtocolMessage<T extends ProtocolMessage> extends Event, SerializerIn<T> {

    /**
     * Gets the operation code (opcode) associated with this message.
     *
     * @return the opcode identifier for this message type
     */
    int getOpCode();

    /**
     * Encodes this message's data into an {@link OutPacket} for transmission over the network.
     *
     * @return a new {@link OutPacket} containing the encoded message data
     */
    @NonNull
    OutPacket encode();

    /**
     * Gets the metadata definition associated with this message type, if available.
     *
     * @return the {@link PacketDefinition} for this message, or {@code null} if not defined
     */
    @Nullable
    default PacketDefinition def() {
      return null;
    }
}
