package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;
import java.util.Stack;
import lombok.Getter;
import lombok.Setter;
import wakfulib.ui.tv.porst.splib.gui.tree.IconNode;

public abstract class AbstractPacketDefinitionNode extends IconNode implements PacketDefinitionNode {

    @Getter @Setter
    protected String name;

    @Getter @Setter
    protected boolean onError = false;

    @Setter
    protected Stack<Object> stackMachine;

    @Override
    public void traverse(ByteBuffer buffer) {
        this.onError = false;
        this.setIcon(null);
    }
}
