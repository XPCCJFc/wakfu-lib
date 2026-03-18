package wakfulib.ui.proxy.model.def.scripting.instructions;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import wakfulib.ui.proxy.model.def.BasicPacketDefinitionNode;
import wakfulib.ui.proxy.model.def.CompositePacketDefinitionContainerNode;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.storage.JavaTypes;

@AllArgsConstructor
public class LoopScriptingInstruction implements ScriptingInstruction {

    private CompositeScriptingInstruction insideLoop;
    private int countLoop;

    public LoopScriptingInstruction(int countLoop) {
        this.countLoop = countLoop;
    }

    @Override
    public ScriptingInstruction copy() {
        LoopScriptingInstruction loopScriptingInstruction = new LoopScriptingInstruction(countLoop);
        loopScriptingInstruction.insideLoop = (CompositeScriptingInstruction)insideLoop.copy();
        return loopScriptingInstruction;
    }

    @Override
    public PacketDefinitionNode toNode() {
        List<PacketDefinitionNode> scriptingInstructions = new ArrayList<>();
        for (int i = 0; i < countLoop; i++) {
            if (insideLoop.getInstructionCount() == 1) {
                ScriptingInstruction next = insideLoop.getInstructions().next();
                PacketDefinitionNode node = next.toNode();
                node.setName(node.getName() + "#" + i);
                scriptingInstructions.add(node);
            } else if (insideLoop.getInstructionCount() > 1){
                PacketDefinitionNode packetDefinitionNode = insideLoop.copy().toNode();
                packetDefinitionNode.setName("#" + i);
                scriptingInstructions.add(packetDefinitionNode);
            }
        }
        if (countLoop == 0 || insideLoop.getInstructionCount() == 0) {
            return new BasicPacketDefinitionNode(JavaTypes.EMPTY, "loop " + countLoop) {
                @Override
                public String toString() {
                    return name;
                }
            };
        }
        return new CompositePacketDefinitionContainerNode("loop " + countLoop, scriptingInstructions.toArray(new PacketDefinitionNode[0]));
    }
}
