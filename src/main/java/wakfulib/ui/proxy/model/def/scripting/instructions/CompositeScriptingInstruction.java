package wakfulib.ui.proxy.model.def.scripting.instructions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Setter;
import wakfulib.ui.proxy.model.def.CompositePacketDefinitionContainerNode;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;

public class CompositeScriptingInstruction implements ScriptingInstruction {

    @Setter
    private String name;
    private final boolean autofold;
    private final List<ScriptingInstruction> instructions = new ArrayList<>();

    public CompositeScriptingInstruction(String name, boolean autofold) {
        this.name = name;
        this.autofold = autofold;
    }

    public CompositeScriptingInstruction(String name) {
        this.name = name;
        this.autofold = false;
    }

    public void addInstruction(ScriptingInstruction instruction) {
        instructions.add(instruction);
    }

    @Override
    public ScriptingInstruction copy() {
        CompositeScriptingInstruction copy = new CompositeScriptingInstruction(name);
        for (ScriptingInstruction instruction : instructions) {
            copy.addInstruction(instruction.copy());
        }
        return copy;
    }

    @Override
    public PacketDefinitionNode toNode() {
        var scriptingInstructions = new ArrayList<PacketDefinitionNode>();
        for (ScriptingInstruction instruction : instructions) {
            scriptingInstructions.add(instruction.toNode());
        }
        return new CompositePacketDefinitionContainerNode(name, scriptingInstructions.toArray(new PacketDefinitionNode[0]));
    }

    public int getInstructionCount() {
        return instructions.size();
    }

    public Iterator<ScriptingInstruction> getInstructions() {
        return instructions.iterator();
    }
}
