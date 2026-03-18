package wakfulib.beans.structure;

import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;

public interface Raw<T extends Raw> extends SelfSerializer<T> {
    default void serialize(OutPacket out) {
        throw new NotImplementedException("Serialize on Raw<?> class");
    }
}
