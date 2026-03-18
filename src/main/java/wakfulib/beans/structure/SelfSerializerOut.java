package wakfulib.beans.structure;

import wakfulib.logic.OutPacket;

public interface SelfSerializerOut extends SerializerOut<SelfSerializerOut> {
    @Override
    default void serialize(SelfSerializerOut data, OutPacket out) {
        data.serialize(out);
    }

    void serialize(OutPacket out);
}
