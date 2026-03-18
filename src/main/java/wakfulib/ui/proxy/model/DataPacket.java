package wakfulib.ui.proxy.model;

import javax.swing.tree.DefaultMutableTreeNode;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.ui.proxy.model.def.PacketDefinition;

public interface DataPacket extends Packet {
    byte[] getData();
    boolean isFromServer();
    PacketDefinition getDef();
    Message<?> getSerializer();
    String getToString();
    int getOpcode();
    void link(Message<?> serializer, PacketDefinition definition);
    void commonDef(DefaultMutableTreeNode root);
}
