package wakfulib.beans.structure;

import wakfulib.logic.OutPacket;

/**
 * Interface for objects that can serialize an object of type T into an {@link OutPacket}.
 *
 * @param <T> the type of the object being serialized
 */
public interface SerializerOut<T> {

    /**
     * Serializes the given data into the provided {@link OutPacket}.
     *
     * @param data the object to serialize
     * @param out the output packet to write the serialized data to
     */
    void serialize(T data, OutPacket out);
}
