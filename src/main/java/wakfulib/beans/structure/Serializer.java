package wakfulib.beans.structure;

/**
 * Interface that combines both {@link SerializerIn} and {@link SerializerOut} for a given type.
 * This interface is for objects that support both serialization and deserialization.
 *
 * @param <T> the type of the object being serialized/deserialized
 */
public interface Serializer<T> extends SerializerIn<T>, SerializerOut<T> {
}
