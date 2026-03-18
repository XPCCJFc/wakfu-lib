package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;

public class CompositePacketDefinitionContainerNode extends AbstractPacketDefinitionNode {

    private final PacketDefinitionNode[] children;
    private int size;

    public CompositePacketDefinitionContainerNode(String name, PacketDefinitionNode... children) {
        if (children.length == 0) throw new IllegalArgumentException("Children of composite definition cannot be empty");
        this.name = name;
        this.children = children;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void traverse(ByteBuffer buffer) {
        super.traverse(buffer);
        removeAllChildren();
        for (PacketDefinitionNode child : children) {
            child.setStackMachine(stackMachine);
            child.traverse(buffer);
            size = size + child.getSize();
            add(child);
        }
    }

    @Override
    public int getOffset() {
        return children[0].getOffset();
    }

    @Override
    public PacketDefinitionNode copy() {
        PacketDefinitionNode[] childrenCopy = new PacketDefinitionNode[children.length];
        for (int i = 0; i < children.length; i++) {
            childrenCopy[i] = children[i].copy();
        }
        return new CompositePacketDefinitionContainerNode(name, childrenCopy);
    }

    @Override
    public Object getValue() {
        return "";
    }

    @Override
    public String toString() {
        return name;
    }
}
