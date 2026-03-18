package wakfulib.ui.proxy.model.def.scripting;

import wakfulib.ui.proxy.model.def.PacketDefinitionNode;

public interface ScriptingInstruction {

    ScriptingInstruction copy();

    PacketDefinitionNode toNode();
}
