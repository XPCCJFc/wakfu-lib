package wakfulib.ui.proxy.model.def.type;

import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter;

import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter.NOT_IMPLEMENTED;

public interface Type extends Traversable {
    String getName();
    default String getJavaType() {
      return "Object";
    }
    default InstructionWriter encoding() {
      return NOT_IMPLEMENTED;
    }

    default InstructionWriter unserialize() {
      return NOT_IMPLEMENTED;
    }
}
