package wakfulib.ui.proxy.model;

import javax.swing.tree.DefaultMutableTreeNode;
import lombok.Getter;
import lombok.Setter;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.ui.proxy.model.def.PacketDefinition;

public class FakePacket implements Packet, Nameable, DataPacket {

    private static int opcodeCounter = 0;
    
    @Getter
    private boolean fromServer;
    private int size;
    @Getter
    private int opcode;
    @Setter
    private String name = "Fake Packet ";
    @Getter
    private final byte[] data;
    @Getter
    private PacketDefinition def;
    private Message<?> serializer;
    
    public FakePacket(boolean fromServer) {
        this(new byte[255], fromServer);
    }
    public FakePacket(byte[] data, boolean fromServer) {
        this.fromServer = fromServer;
        this.data = data;
        def = PacketDefinition.UNKNOWN_DEF;
        this.size = data.length;
        opcode = opcodeCounter++;
        name = name + opcode;
    }

    private void updateCommon() {
        byte counter = 0;
        size = (((data[counter++] & 0xFF) << 8) | (data[counter++] & 0xFF));
        if (! fromServer) {
            counter++;
        }
        opcode = (short)(((data[counter++] & 0xFF) << 8) | (data[counter] & 0xFF));
    }

    public void setFromServer(boolean isFromServer) {
        fromServer = isFromServer;
        updateCommon();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Message<?> getSerializer() {
        return serializer;
    }

    @Override
    public String getToString() {
        return name;
    }

    @Override
    public void link(Message<?> serializer, PacketDefinition definition) {
        this.serializer = serializer;
        this.def = definition;
    }

    @Override
    public void commonDef(DefaultMutableTreeNode root) {

    }
}
