package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.tree.MutableTreeNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import wakfulib.ui.tv.porst.splib.gui.tree.WakfuLibTreeCellRenderer;

public class RuntimeCompositePacketDefinitionContainerNode extends AbstractPacketDefinitionNode {
    
    @Getter
    @AllArgsConstructor
    public static class CurrentScriptingState {
        private final ByteBuffer buffer;
        private final Stack<Object> stack;
    }

    private final PacketDefinitionNode children;
    private final Function<CurrentScriptingState, Predicate<Integer>> shouldContinueLooping;
    private final boolean foldIteration;
    private int size;

    private int offset;

    private String lastCountLoop;

    public RuntimeCompositePacketDefinitionContainerNode(String name, PacketDefinitionNode child, boolean foldIteration, Function<CurrentScriptingState, Predicate<Integer>> shouldContinueLooping) {
        this.foldIteration = foldIteration;
        this.lastCountLoop = foldIteration ? "" : " X";
        this.name = name;
        this.children = child;
        this.shouldContinueLooping = shouldContinueLooping;
        this.offset = 0;
        setIcon(WakfuLibTreeCellRenderer.EXPAND_EMPTY_ICON);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void traverse(ByteBuffer buffer) {
        super.traverse(buffer);
        removeAllChildren();
        int counter = 0;
        Predicate<Integer> shouldContinue = shouldContinueLooping.apply(new CurrentScriptingState(buffer, stackMachine));
        List<PacketDefinitionNode> toAdd = new ArrayList<>(1);
        while (shouldContinue.test(counter)) {
            PacketDefinitionNode copy = children.copy();
            copy.setStackMachine(stackMachine);
            copy.traverse(buffer);
            copy.setName(copy.getName() + counter);
            counter++;
            size = size + copy.getSize();
            toAdd.add(copy);

            if (offset == 0) {
                offset = copy.getOffset();
            }
        }
        if (counter == 0) setIcon(WakfuLibTreeCellRenderer.EXPAND_EMPTY_ICON);
        if (foldIteration && toAdd.size() == 1) {
            var packetDefinitionNode = toAdd.get(0);
            var childCount = packetDefinitionNode.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                add((MutableTreeNode) packetDefinitionNode.getChildAt(0));
            }
        } else {
            for (var packetDefinitionNode : toAdd) {
                add(packetDefinitionNode);
            }
        }
        if (foldIteration) {
            lastCountLoop = "";
        } else {
            lastCountLoop = " " + counter;
        }
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public PacketDefinitionNode copy() {
        return new RuntimeCompositePacketDefinitionContainerNode(name, children.copy(), foldIteration, shouldContinueLooping);
    }

    @Override
    public Object getValue() {
        return "";
    }

    @Override
    public String toString() {
        return name + lastCountLoop;
    }
}

