package wakfulib.ui.proxy.model.def.type;

import wakfulib.ui.utils.RuntimeObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CompositeType extends SizeVariableType {
    private final List<Type> children = new ArrayList<>();

    public CompositeType(String name) {
        super(name, null);
    }


    @Override
    public TypeInstance traverse(ByteBuffer buffer, Stack<Object> stack, Object... args) {
        RuntimeObject res = new RuntimeObject(this.getName());
        int totalSize = 0;
        for (Type child : children) {
            TypeInstance traverse = child.traverse(buffer, stack, args);
            totalSize = totalSize + traverse.size();
            res.addFields(child.getName(), traverse);
        }
        return new TypeInstance(totalSize, res);
    }

    public void addType(Type type) {
        children.add(type);
    }
}
