package wakfulib.ui.proxy.model.def.type;

import java.nio.ByteBuffer;
import java.util.Stack;

@FunctionalInterface
public interface Traversable {
    TypeInstance traverse(ByteBuffer buffer, Stack<Object> stack, Object... args);
}
