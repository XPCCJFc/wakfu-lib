package wakfulib.ui.proxy.model;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import wakfulib.beans.structure.SerializerIn;
import wakfulib.doc.NonNull;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.ui.proxy.model.def.BasicPacketDefinitionNode;
import wakfulib.ui.proxy.model.def.PacketDefinition;
import wakfulib.ui.proxy.model.def.type.storage.JavaTypes;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class WakfuPacket implements Packet, DataPacket, FileLinkable, Nameable {

    private static final Options OPTIONS = Settings.getInstance().getOptions();
    private byte archTarget;

    private String toString;

    @Setter
    private String file;

    private final boolean fromServer;

    @Getter(onMethod = @__({@NonNull}))
    private PacketDefinition def;

    private boolean edited;

    private final byte[] data;

    private final int size;

    private final int opcode;

    private LocalDateTime time;
    private Message<?> serializer;
    private boolean fake = false;

    public WakfuPacket(ByteBuf buffer, boolean fromServer) {
        edited = false;
        time = LocalDateTime.now();
        this.fromServer = fromServer;
        data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        byte counter = 0;
        size = (((data[counter++] & 0xFF) << 24) |
            ((data[counter++] & 0xFF) << 16) |
            ((data[counter++] & 0xFF) << 8)  |
            (data[counter++] & 0xFF));;
        if (! fromServer) {
            archTarget = data[counter++];
        }
        opcode = (short)(((data[counter++] & 0xFF) << 8) | (data[counter] & 0xFF));
        def = PacketDefinition.UNKNOWN_DEF;
        setEdited(false);
    }

    public WakfuPacket(LocalDateTime time, ByteBuf buffer, boolean fromServer) {
        this(buffer, fromServer);
        this.time = time;
    }

    public void link(Message<?> serializer, PacketDefinition def) {
        this.serializer = serializer;
        this.def = def;
        updateName(def.getName());
    }

    public void setName(String name) {
        fake = true;
        def = new PacketDefinition(name);
        updateName(def.getName());
    }

    private void updateName(String name) {
        toString = (fromServer ? "< " : "> ") + name + '{' + opcode + '}' + (edited ? "* (" : " (") + size + ')' + (fromServer ? "" : (" [" + archTarget + "]"));
    }

    @Override
    public String toString() {
        if (OPTIONS.isDISPLAY_TIMESTAMP()) {
            return '[' + time.format(DateTimeFormatter.ISO_TIME) + "] " + toString;
        } else {
            return toString;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WakfuPacket that = (WakfuPacket) o;

        if (fromServer != that.fromServer) return false;
        if (edited != that.edited) return false;
        if (size != that.size) return false;
        if (opcode != that.opcode) return false;
        if (fake != that.fake) return false;
        if (! toString.equals(that.toString)) return false;
        return time.equals(that.time);
    }

    @Override
    public int hashCode() {
        int result = toString.hashCode();
        result = 31 * result + (fromServer ? 1 : 0);
        result = 31 * result + (edited ? 1 : 0);
        result = 31 * result + size;
        result = 31 * result + opcode;
        result = 31 * result + time.hashCode();
        result = 31 * result + (fake ? 1 : 0);
        return result;
    }

    public Message<?> unserialize() {
      return serializerChainWithoutHeader(serializer);
    }

    public <T> T serializerChainWithoutHeader(SerializerIn<T> serializer) {
      ByteBuffer wrapped = ByteBuffer.wrap(data);
      wrapped.position(wrapped.position() + (! fromServer ? 5 : 4));
      return serializer.unserialize(wrapped);
    }

    public boolean isKnown() {
        return serializer != null;
    }

    @Override
    public void commonDef(DefaultMutableTreeNode root) {
        root.add(new BasicPacketDefinitionNode(JavaTypes.INTEGER, "size"));
        if (! fromServer) {
            root.add(new BasicPacketDefinitionNode(JavaTypes.BYTE, "archTarget"));
        }
        root.add(new BasicPacketDefinitionNode(JavaTypes.SHORT, "opcode"));
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
        updateName(PacketDefinition.UNKNOWN_DEF == def ? "" : def.getName());
    }
}
