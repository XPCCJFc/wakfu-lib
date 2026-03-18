package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;
import java.util.Stack;
import javax.swing.Icon;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public interface PacketDefinitionNode extends MutableTreeNode {
    int getSize();

    void traverse(ByteBuffer buffer);

    TreeNode[] getPath();

    int getOffset();

    PacketDefinitionNode copy();

    Object getValue();

    void setStackMachine(Stack<Object> stackMachine);

    default PacketDefinitionNode getRedirection() {
        return null;
    }

    void setName(String name);

    String getName();

    void setOnError(boolean onError);

    boolean isOnError();

    void setIcon(Icon icon);
}
