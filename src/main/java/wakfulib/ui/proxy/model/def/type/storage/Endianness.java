package wakfulib.ui.proxy.model.def.type.storage;

import java.nio.ByteOrder;
import wakfulib.ui.proxy.model.def.type.FixedSizedType;
import wakfulib.ui.proxy.model.def.type.Type;

public final class Endianness {

    public static final Type BIG_ENDIAN = new FixedSizedType(0, "BIG_ENDIAN", b -> {
        b.order(ByteOrder.BIG_ENDIAN);
        return null;
    });

    public static final Type LITTLE_ENDIAN = new FixedSizedType(0, "LITTLE_ENDIAN", b -> {
        b.order(ByteOrder.LITTLE_ENDIAN);
        return null;
    });

    public static final Type[] values = new Type[] {
        BIG_ENDIAN, LITTLE_ENDIAN
    };
}
