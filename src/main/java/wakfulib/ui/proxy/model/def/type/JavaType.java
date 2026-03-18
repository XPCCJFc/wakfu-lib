package wakfulib.ui.proxy.model.def.type;

import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.getterFor;
import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.setterFor;

import java.nio.ByteBuffer;
import java.util.function.Function;
import wakfulib.utils.StringUtils;

public class JavaType extends FixedSizedType {

    public JavaType(int size, String name, Function<ByteBuffer, Object> simpleTraverseFunction) {
        super(size, name, simpleTraverseFunction,
            (ctx) -> ctx.resName + ".write" + StringUtils.capitalize(name.toLowerCase()) + "(" + getterFor(ctx.fieldName) + ");",
            ((ctx) -> ctx.resName + "." + setterFor(ctx.fieldName, ctx.argName + ".get" + StringUtils.capitalize(name.toLowerCase()) + "()") + ";"));
    }

    @Override
    public String getJavaType() {
        return getName().toLowerCase();
    }
}
