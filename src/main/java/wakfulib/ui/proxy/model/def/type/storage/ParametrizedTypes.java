package wakfulib.ui.proxy.model.def.type.storage;

import static wakfulib.ui.proxy.model.def.scripting.ScriptingModel.getPositiveComputedArg;
import static wakfulib.utils.StringUtils.capitalize;

import java.nio.ByteBuffer;
import wakfulib.ui.proxy.model.def.type.SizeVariableType;
import wakfulib.ui.proxy.model.def.type.Type;
import wakfulib.utils.StringUtils;

public final class ParametrizedTypes {

    public static final Type PROTO = new SizeVariableType("PROTO", ((atomicInteger, byteBuffer, s, args) -> {
        try {
            var string = (String) args[0];
            Class<?> protoClass = Class.forName("wakfulib.protobuf." + string.replace(".", "$"));
            var parsed = protoClass.getMethod("parseFrom", ByteBuffer.class).invoke(null, byteBuffer);
            atomicInteger.addAndGet(byteBuffer.remaining());
            return parsed;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }), "PROTO");

    //TODO c'est un peu moche on pourrais avoir à la compilation un type paramétré de taille fini
    // a la place d'avoir un type variable et paramétré a l'execution
    public static final Type X_STRING = new SizeVariableType("xSTRING", ((atomicInteger, byteBuffer, s, args) -> {
        try {
            if (args.length == 0)
                throw new RuntimeException("xSTRING: Was expecting one parameter (type: math expr) found 0");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[0]).append(" ");
            }
            int how = getPositiveComputedArg(sb.toString().trim(), -1);
            var data = new byte[how];
            byteBuffer.get(data);
            atomicInteger.addAndGet(how);
            return StringUtils.fromUTF8(data);
        } catch (Exception e) {
            throw new RuntimeException("While computing xSTRING", e);
        }
    }), "$String");

    public static final Type X_BUFFER = new SizeVariableType("xBUFFER", ((atomicInteger, byteBuffer, s, args) -> {
        try {
            if (args.length == 0)
                throw new RuntimeException("Was expecting one parameter (type: math expr) found 0");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[0]).append(" ");
            }
            int how = getPositiveComputedArg(sb.toString().trim(), -1);
            var data = new byte[how];
            byteBuffer.get(data);
            atomicInteger.addAndGet(how);
            return StringUtils.fromUTF8(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }), "$byte[]");

    public static final Type ID_ENUM = new SizeVariableType("idEnum", ((atomicInteger, byteBuffer, s, args) -> {
        try {
            if (args.length < 2)
                throw new RuntimeException("Was expecting 2 (type: (int size, string enumFullName)) found 0");

            String method = "id";
            if (args.length == 3) {
                method = args[2].toString();
            }

            var how = Integer.parseInt(args[0].toString());
            atomicInteger.addAndGet(how);
            if (how == 0) {
                throw new RuntimeException("Cannot read an enum " + method + " of size 0");
            }
            long enumId;
            if (how == 1) {
                enumId = byteBuffer.get();
            } else if (how == 2) {
                enumId = byteBuffer.getShort();
            } else if (how == 4) {
                enumId = byteBuffer.getInt();
            } else if (how == 8) {
                enumId = byteBuffer.getLong();
            } else {
                throw new RuntimeException("Cannot read an enum " + method + " of size " + how);
            }

            Class<?> paramClass = Class.forName(args[1].toString());
            if (!paramClass.isEnum()) {
                throw new RuntimeException("Class " + paramClass + " should be an enum");
            }

            var getId = paramClass.getMethod("get" + capitalize(method));
            for (Object enumConstant : paramClass.getEnumConstants()) {
                var obj = getId.invoke(enumConstant);
                boolean found;
                if (! obj.getClass().isPrimitive()) {
                    found = enumId == ((Number) obj).longValue();
                } else {
                    found = enumId == (long) obj;
                }
                if (found) {
                    return ((Enum<?>) enumConstant).name() + " [" + enumId + "]";
                }
            }
            return "?? [" + enumId + "]";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }), "$$$");

    public static final Type[] values = new Type[] {PROTO, X_BUFFER, X_STRING, ID_ENUM};
}
