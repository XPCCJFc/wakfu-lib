package wakfulib.ui.proxy.model.def.scripting.instructions;

import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import wakfulib.ui.proxy.model.def.BasicPacketDefinitionNode;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.model.def.RuntimeCompositePacketDefinitionContainerNode;
import wakfulib.ui.proxy.model.def.RuntimeCompositePacketDefinitionContainerNode.CurrentScriptingState;
import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.storage.JavaTypes;

@AllArgsConstructor
public class RuntimeLoopScriptingInstruction implements ScriptingInstruction {

    private final String name;
    private final boolean foldIteration;
    private CompositeScriptingInstruction insideLoop;
    private final Function<CurrentScriptingState, Predicate<Integer>> shouldContinueLoopingBuilder;
    
    public RuntimeLoopScriptingInstruction(String name, Function<CurrentScriptingState, Predicate<Integer>> shouldContinueLoopingBuilder) {
        this(name, false, shouldContinueLoopingBuilder);
    }
    
    public RuntimeLoopScriptingInstruction(String name, boolean foldIteration, Function<CurrentScriptingState, Predicate<Integer>> shouldContinueLoopingBuilder) {
        this.name = name;
        this.shouldContinueLoopingBuilder = shouldContinueLoopingBuilder;
        this.foldIteration = false;
    }

    @Override
    public ScriptingInstruction copy() {
        RuntimeLoopScriptingInstruction loopScriptingInstruction = new RuntimeLoopScriptingInstruction(name, foldIteration, shouldContinueLoopingBuilder);
        loopScriptingInstruction.insideLoop = (CompositeScriptingInstruction)insideLoop.copy();
        return loopScriptingInstruction;
    }

    @Override
    public PacketDefinitionNode toNode() {
        int instructionCount = insideLoop.getInstructionCount();
        PacketDefinitionNode inside;
        if (instructionCount == 1) {
            ScriptingInstruction next = insideLoop.getInstructions().next();
            PacketDefinitionNode node = next.toNode();
            node.setName(node.getName() + "#");
            inside = node;
        } else if (instructionCount > 1){
            PacketDefinitionNode packetDefinitionNode = insideLoop.copy().toNode();
            packetDefinitionNode.setName("#");
            inside = packetDefinitionNode;
        } else {
            return new BasicPacketDefinitionNode(JavaTypes.EMPTY, name + (foldIteration ? "" : " X") + " (empty)") {
                @Override
                public String toString() {
                    return name;
                }
            };
        }
        return new RuntimeCompositePacketDefinitionContainerNode(name, inside, foldIteration, shouldContinueLoopingBuilder);
    }
}
