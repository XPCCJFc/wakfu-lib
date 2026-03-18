package wakfulib.beans.structure;

import wakfulib.logic.OutPacket;

/**
 * Interface for objects that can serialize themselves into an {@link OutPacket}.
 *
 * @param <T> the type of the object that can serialize itself
 */
public interface SelfSerializer<T extends SelfSerializer> extends Serializer<T> {

    /**
     * Serializes this instance into the provided {@link OutPacket}.
     *
     * @param out the output packet to write the serialized data to
     */
    void serialize(OutPacket out);

    @Override
    default void serialize(T data, OutPacket out) {
        data.serialize(out);
    }
}
