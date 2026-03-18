package wakfulib.ui.proxy.model.def;

import java.lang.reflect.Field;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.Getter;
import wakfulib.internal.Version;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.internal.versionable.protocol.ToServerMessage;

/**
 * Represents a packet structure
 *
 * It is only use by the GUI to interpret raw data.
 */
public class PacketDefinition {

    public static final PacketDefinition UNKNOWN_DEF = new PacketDefinition("???");

    @Getter
    private final String name;
    public DefaultMutableTreeNode defRoot;

    public PacketDefinition(Class<?> clazz) {
        name = clazz.getSimpleName();
        defRoot = new DefaultMutableTreeNode();
    }

    public PacketDefinition(String name) {
        this.name = name;
    }

    public static PacketDefinition fromClass(Class<?> clazz) {
        PacketDefinition packetDefinition = new PacketDefinition(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            convertField(packetDefinition, field);
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (Message.class.isAssignableFrom(clazz.getSuperclass()) && superclass != ToClientMessage.class && superclass != ToServerMessage.class) {
            for (Field field : superclass.getDeclaredFields()) {
                convertField(packetDefinition, field);
            }
        }
        return packetDefinition;
    }

    private static void convertField(PacketDefinition packetDefinition, Field field) {
        var annotation = Version.getRangeForCurrentVersion(field);
        if (annotation == null || Version.getCurrent().isInRange(annotation)) {
            PacketDefinitionNode built = PacketDefinitionNodeFactory.fromClass(field);
            if (built != null) {
                packetDefinition.defRoot.add(built);
            }
        }
    }

    public void add(PacketDefinitionNode node) {
        defRoot.add(node);
    }
}
