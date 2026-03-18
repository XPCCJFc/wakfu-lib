package wakfulib.internal.versionable;

import wakfulib.internal.Version;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.internal.versionable.protocol.OpCode;
import wakfulib.logic.Context;
import wakfulib.logic.OutPacket;

public class PacketFactory {

    public static int findOpCode(Class<? extends Message> messageClass) {
        Version current = Version.getCurrent();
        OpCode declaredAnnotation = messageClass.getDeclaredAnnotation(OpCode.class);
        if (declaredAnnotation != null) {
            if (declaredAnnotation.version() == current) {
                return declaredAnnotation.value();
            }
        } else {
            OpCode.OpCodes opCodes = messageClass.getDeclaredAnnotation(OpCode.OpCodes.class);
            if (opCodes != null) {
                for (OpCode code : opCodes.value()) {
                    if (code.version() == current) {
                        return code.value();
                    }
                }
            }
        }
        return Message.UNKNOWN_OPCODE;
    }

    public static OutPacket outPacket(Context context, int opcode) {
        return new OutPacket(context == Context.Server, opcode);
    }
}
