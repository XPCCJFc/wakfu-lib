package wakfulib.ui.proxy.model.def.type;

import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.function.Function;
import lombok.Getter;
import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter;

public class FixedSizedType implements Type {
    @Getter
    private final int size;
    @Getter
    private final String name;
    private final Traversable traverseFunction;
    private final InstructionWriter encoding;
    private final InstructionWriter unserialize;

    public FixedSizedType(int size, String name, Function<ByteBuffer, Object> simpleTraverseFunction) {
        this(size, name, simpleTraverseFunction, InstructionWriter.NOT_IMPLEMENTED, InstructionWriter.NOT_IMPLEMENTED);
    }

    public FixedSizedType(int size, String name, Function<ByteBuffer, Object> simpleTraverseFunction, InstructionWriter encoding, InstructionWriter unserialize) {
        this(size, name, (b, s, a) -> {
            var apply = simpleTraverseFunction.apply(b);
            s.push(apply);
            return new TypeInstance(size, apply);
        }, encoding, unserialize);
    }

    public FixedSizedType(int size, String name, Traversable traverseFunction) {
        this(size, name, traverseFunction, InstructionWriter.NOT_IMPLEMENTED, InstructionWriter.NOT_IMPLEMENTED);
    }

    public FixedSizedType(int size, String name, Traversable traverseFunction, InstructionWriter encoding, InstructionWriter unserialize) {
        this.size = size;
        this.name = name;
        this.traverseFunction = traverseFunction;
        this.encoding = encoding;
        this.unserialize = unserialize;
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

    @Override
    public String getJavaType() {
        return name.toLowerCase();
    }
}
