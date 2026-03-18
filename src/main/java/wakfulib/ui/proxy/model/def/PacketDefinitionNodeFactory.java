package wakfulib.ui.proxy.model.def;

import java.lang.reflect.Field;
import wakfulib.internal.Structure;
import wakfulib.internal.Version;
import wakfulib.ui.proxy.model.def.type.storage.ComplexeTypes;
import wakfulib.ui.proxy.model.def.type.storage.JavaTypes;

public final class PacketDefinitionNodeFactory {

    public static PacketDefinitionNode versionMMR() {
        return new CompositePacketDefinitionNode("version",
            args -> Version.getVersion((Byte) args[0], (Short) args[1], (Byte) args[2]),
            new BasicPacketDefinitionNode(JavaTypes.BYTE, "major"),
            new BasicPacketDefinitionNode(JavaTypes.SHORT, "minor"),
            new BasicPacketDefinitionNode(JavaTypes.BYTE, "revision")
        );
    }

    public static PacketDefinitionNode fromClass(Field field) {
        Class<?> clazz = field.getType();
        String name = field.getName();

        if (clazz == Integer.class || clazz == int.class) {
            return new BasicPacketDefinitionNode(JavaTypes.INTEGER, name);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return new BasicPacketDefinitionNode(JavaTypes.BOOLEAN, name);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return new BasicPacketDefinitionNode(JavaTypes.BYTE, name);
        } else if (clazz == Short.class || clazz == short.class) {
            return new BasicPacketDefinitionNode(JavaTypes.SHORT, name);
        } else if (clazz == Long.class || clazz == long.class) {
            return new BasicPacketDefinitionNode(JavaTypes.LONG, name);
        } else if (clazz == Float.class || clazz == float.class) {
            return new BasicPacketDefinitionNode(JavaTypes.FLOAT, name);
        } else if (clazz == Double.class || clazz == double.class) {
            return new BasicPacketDefinitionNode(JavaTypes.DOUBLE, name);
        } else if (clazz == String.class) {
            final Structure annotation = field.getAnnotation(Structure.class);
            if (annotation != null) {
                switch (annotation.stringSize()) {
                    case BYTE:
                        return new BasicPacketDefinitionNode(ComplexeTypes.BYTE_STRING, name);
                    case SHORT:
                        return new BasicPacketDefinitionNode(ComplexeTypes.SHORT_STRING, name);
                    case INTEGER:
                        return new BasicPacketDefinitionNode(ComplexeTypes.INT_STRING, name);
                }
            }
        }
        return null;
    }
}
