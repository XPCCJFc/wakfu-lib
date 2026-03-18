package wakfulib.ui.proxy.view.packetview;

import java.awt.Color;
import java.awt.Component;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.Stack;
import javax.swing.tree.TreeNode;

import wakfulib.doc.Nullable;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;

public interface PacketView {

    Color SUCCESS_COLOR = new Color(135, 241, 153);

    void select(DataPacket selectedValue);

    void clear();

    Component getComponent();

    DataPacket getSelectedValue();
    default AnalyseResult analyse(Enumeration<TreeNode> children) {
        return analyse(children, null);
    }

    default AnalyseResult analyse(Enumeration<TreeNode> children, @Nullable ByteOrder ordering) {
        ByteBuffer bb = ByteBuffer.wrap(getSelectedValue().getData());
        if (ordering != null) {
            bb.order(ordering);
        }
        Stack<Object> stackMachine = new Stack<>();
        PacketDefinitionNode nextElement = null;
        try {
            PacketDefinitionNode redirection;
            while (children.hasMoreElements()) {
                do {
                    Object next = children.nextElement();
                    if (! (next instanceof PacketDefinitionNode)) {
                        new AnalyseResult(new IllegalStateException("Unsupported packet type: " + next.getClass().getSimpleName()), nextElement, bb);
                    }
                    nextElement = (PacketDefinitionNode) next;
                    nextElement.setStackMachine(stackMachine);
                    nextElement.traverse(bb);
                    stackMachine.push(nextElement.getValue());
                    redirection = nextElement.getRedirection();
                } while (redirection != null);
            }
        } catch (Exception e) {
            return new AnalyseResult(e, nextElement, bb);
        }
        return new AnalyseResult(null, nextElement, bb);
    }



    default void onExit() {

    }
}
