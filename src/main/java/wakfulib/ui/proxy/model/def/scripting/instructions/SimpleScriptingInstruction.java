package wakfulib.ui.proxy.model.def.scripting.instructions;


import static wakfulib.utils.StringUtils.getIdentifier;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import wakfulib.doc.NonNull;
import wakfulib.ui.proxy.model.def.BasicPacketDefinitionNode;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.Type;

@Getter @Setter
public class SimpleScriptingInstruction implements ScriptingInstruction {

    public boolean unused = false;
    private Type type;

    private String label;

    private Object[] args;

    public SimpleScriptingInstruction(Type type, String label, Object[] args) {
        this.type = type;
        this.label = label;
        this.args = args;
    }

    public PacketDefinitionNode toNode() {
        return new BasicPacketDefinitionNode(type, label,args);
    }

    public SimpleScriptingInstruction copy() {
        return new SimpleScriptingInstruction(type, label, args);
    }
    
    private static final AtomicInteger i = new AtomicInteger(0);
    private String lastJavaId = null;
    @NonNull
    public String javaId() {
        if (lastJavaId == null) {
            if (label.isEmpty()) {
                if (i.get() > 99) {
                    i.set(0);
                }
                lastJavaId = "generated" + i.incrementAndGet();
            } else {
                lastJavaId = getIdentifier(label);
            }
        }
        return lastJavaId;
    }
}
