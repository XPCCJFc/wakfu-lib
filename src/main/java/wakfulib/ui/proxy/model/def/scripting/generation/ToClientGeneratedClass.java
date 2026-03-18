package wakfulib.ui.proxy.model.def.scripting.generation;

import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;

import java.util.Arrays;
import java.util.List;

public class ToClientGeneratedClass extends GeneratedClass {


  public ToClientGeneratedClass(String name, int op, Iterable<ScriptingInstruction> structure, Byte archTarget, String packageName, String version) {
    super(packageName, name, op, structure, null, version);
  }

  public ToClientGeneratedClass(String name, int op, Iterable<ScriptingInstruction> structure, String packageName, String version) {
    super(packageName, name, op, structure, null, version);
  }

  public String getTemplate() {
    return """
        package %package%;

        %imports%

        @Getter
        @Setter
        @VersionDependant
        public abstract class %className% extends ToClientMessage<%className%> {
            %fields%
            @VersionRange(min = Version.v%version%)
            @OpCode(version = Version.v%version%, value = %op%)
            static class %className%V1 extends %className% {
    
                @Override
                public %className% unserialize(@NonNull ByteBuffer buffer) {
                    %className% res = new %className%V1();
                    %unserialize%return res;
                }
        
                @Override
                public @NonNull OutPacket encode() {
                    OutPacket o = getOutPacket();
                    %encode%return o;
                }
            }
        }
        """;
  }

  public List<String> getTemplateImports() {
    return Arrays.asList(
        "import java.nio.ByteBuffer;",
        "import lombok.Getter;",
        "import lombok.Setter;",
        "import wakfulib.doc.NonNull;",
        "import wakfulib.internal.Version;",
        "import wakfulib.internal.VersionRange;",
        "import wakfulib.internal.registration.VersionDependant;",
        "import wakfulib.internal.versionable.protocol.OpCode;",
        "import wakfulib.internal.versionable.protocol.ToClientMessage;",
        "import wakfulib.logic.OutPacket;"
    );
  }

  public String getDefaultPackage() {
    return "wakfulib.internal.versionable.protocol.toClient";
  }


}
