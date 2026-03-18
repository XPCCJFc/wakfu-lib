package wakfulib.ui.proxy.model.def.type;

import lombok.Getter;
import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter;

import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter.NOT_IMPLEMENTED;

public class SizeVariableType implements Type {

    @Getter
    private final String name;

    private final InstructionWriter encoding;
    private final InstructionWriter unserialize;

    private Traversable traverseFunction;

    @Getter
    private final String javaType;
    @Getter
    private SizeAwareTraversable notKnownSizeType;

    public SizeVariableType(String name, String javaType) {
        this(name, null, NOT_IMPLEMENTED, NOT_IMPLEMENTED, javaType);
    }

    public SizeVariableType(String name, SizeAwareTraversable notKnownSizeType, String javaType) {
        this(name, notKnownSizeType, NOT_IMPLEMENTED, NOT_IMPLEMENTED, javaType);
    }

    public SizeVariableType(String name, SizeAwareTraversable notKnownSizeType,
                            InstructionWriter unserialize, InstructionWriter encoding, String javaType) {
        this.name = name;
        this.encoding = encoding;
        this.unserialize = unserialize;
        this.javaType = javaType;
        setTraverseFunction(notKnownSizeType);
    }

    @Override
    public TypeInstance traverse(ByteBuffer buffer, Stack<Object> stack, Object... args) {
        return traverseFunction.traverse(buffer, stack, args);
    }

    @Override
    public InstructionWriter encoding() {
        return encoding;
    }

    @Override
    public InstructionWriter unserialize() {
        return unserialize;
    }

    private void setTraverseFunction(SizeAwareTraversable notKnownSizeType) {
        this.notKnownSizeType = notKnownSizeType;
        if (notKnownSizeType == null) {
            traverseFunction = (b, s, a) -> {throw new IllegalStateException("Traverse function is not defined !");};
        } else {
            traverseFunction = (b, s, a) -> {
                AtomicInteger i = new AtomicInteger(0);
                Object res = notKnownSizeType.traverse(i, b, s, a);
                return new TypeInstance(i.get(), res);
            };
        }
    }
}
