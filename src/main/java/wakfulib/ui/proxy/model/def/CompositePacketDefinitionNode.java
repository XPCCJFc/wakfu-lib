package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class CompositePacketDefinitionNode extends AbstractPacketDefinitionNode {
    private final PacketDefinitionNode[] children;
    private final Function<Object[], Object> constructor;
    private final Object[] args;
    private Object value;
    private int totalSize;

    public CompositePacketDefinitionNode(String name, Function<Object[], Object> constructor, PacketDefinitionNode... children) {
        this.constructor = constructor;
        if (children.length == 0) throw new IllegalArgumentException("Children of composite definition cannot be empty");
        this.args = new Object[children.length];
        this.name = name;
        this.children = children;
        totalSize = 0;
        for (PacketDefinitionNode child : children) {
            add(child);
            totalSize = totalSize + child.getSize();
        }
    }

    @Override
    public int getSize() {
        return totalSize;
    }

    @Override
    public void traverse(ByteBuffer buffer) {
        super.traverse(buffer);
        for (PacketDefinitionNode child : children) {
            child.traverse(buffer);
        }
        for (int i = 0; i < children.length; i++) {
            args[i] = children[i].getValue();
        }
        value = constructor.apply(args);
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
        return new CompositePacketDefinitionNode(name, constructor, childrenCopy);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
