package wakfulib.ui.proxy.model.def.scripting.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter.InstructionContext;

@FunctionalInterface
public interface InstructionWriter extends Function<InstructionContext, String> {

  InstructionWriter NOT_IMPLEMENTED = ctx -> "//TODO implement me " + ctx.fieldName;

  @AllArgsConstructor
  class InstructionContext {
      public final String argName;
      public final String resName;
      public final String fieldName;

      public final List<String> additionalImports = new ArrayList<>();
  }
}
