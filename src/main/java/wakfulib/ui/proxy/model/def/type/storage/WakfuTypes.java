package wakfulib.ui.proxy.model.def.type.storage;

import wakfulib.ui.proxy.model.def.type.SizeVariableType;
import wakfulib.ui.proxy.model.def.type.Type;
import wakfulib.utils.StringUtils;
public final class WakfuTypes {

    public static final Type ENCODED_INT = new SizeVariableType( "EncodedInt",
        (i, buffer, s, args) -> {
            byte encryptedSize = buffer.get();
            i.addAndGet(encryptedSize + 1);
            byte[] encryptedKeyBuffer = new byte[encryptedSize];
            buffer.get(encryptedKeyBuffer);
            return Integer.decode(StringUtils.fromUTF8(encryptedKeyBuffer));
        }, "int");
}
