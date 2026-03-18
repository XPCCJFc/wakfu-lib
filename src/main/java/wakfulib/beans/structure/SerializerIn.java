package wakfulib.beans.structure;

import java.nio.ByteBuffer;
import wakfulib.doc.NonNull;
import wakfulib.exception.DeserializationException;

/**
 * Interface for objects that can be deserialized from a byte buffer.
 *
 * @param <T> the type of the object being deserialized
 */
public interface SerializerIn<T> {

    /**
     * Deserializes an object of type T from the provided {@link ByteBuffer}.
     *
     * @param buffer the buffer containing the serialized data
     * @return a new instance of type T populated with the data from the buffer
     * @throws DeserializationException if the data in the buffer is invalid or incomplete
     */
    T unserialize(@NonNull ByteBuffer buffer) throws DeserializationException;

    /**
     * Deserializes an object of type T from the provided byte array.
     *
     * @param data the byte array containing the serialized data
     * @return a new instance of type T populated with the data from the array
     */
    default T unserialize(byte[] data) {
        return unserialize(ByteBuffer.wrap(data));
    }
}
