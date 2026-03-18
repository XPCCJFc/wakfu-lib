package wakfulib.ui.proxy.model.def;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import wakfulib.ui.proxy.model.def.type.Type;
import wakfulib.ui.proxy.model.def.type.TypeInstance;

public class BasicPacketDefinitionNode extends AbstractPacketDefinitionNode {

    protected final Type type;

    @Setter @Getter
    protected String name;
    private final Object[] args;

    @Getter @Setter
    public Object value;
    @Getter
    public int size;
    @Getter
    private int offset;

    @Getter @Setter
    private boolean onError = false;


    public BasicPacketDefinitionNode(Type type, String name) {
        this.type = type;
        this.name = name;
        this.args = null;
    }

    public BasicPacketDefinitionNode(Type type, String name, Object... args) {
        this.type = type;
        this.name = name;
        this.args = args;
    }

    @Override
    public String toString() {
        if (value instanceof byte[]) {
            return name + ": [" + ((byte[]) value).length + "] (" + type.getName() + ") ";
        }
        return name + ": " + value + " (" + type.getName() + ") ";
    }


    @Override
    public void traverse(ByteBuffer buffer) {
        super.traverse(buffer);
        offset = buffer.position();
        TypeInstance traverse = type.traverse(buffer, stackMachine, args);
        size = traverse.size();
        value = traverse.value();
    }

    @Override
    public PacketDefinitionNode copy() {
        BasicPacketDefinitionNode res = new BasicPacketDefinitionNode(type, name, args);
        res.offset = offset;
        res.value = value;
        res.size = size;
        return res;
    }
}
