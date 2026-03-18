package wakfulib.ui.proxy.model.def.type;

import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
public interface SizeAwareTraversable {
    Object traverse(AtomicInteger sizeCounter, ByteBuffer buffer, Stack<Object> stack, Object... args);
}
