package wakfulib.ui.proxy.model.def.type.storage;

import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.getterFor;
import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.getterForBoolean;
import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.setterFor;

import java.nio.ByteBuffer;
import wakfulib.ui.proxy.model.def.type.FixedSizedType;
import wakfulib.ui.proxy.model.def.type.JavaType;
import wakfulib.ui.proxy.model.def.type.Type;

public final class JavaTypes {

    public static final Type EMPTY = new FixedSizedType(0,"EMPTY", b -> null,
        ((ctx) -> null),  (ctx) -> null);
    public static final Type BYTE = new FixedSizedType(1, "BYTE" , b -> b.get(),
        (ctx) -> ctx.resName + ".writeByte(" + getterFor(ctx.fieldName) + ");",
        ((ctx) -> ctx.resName + "." + setterFor(ctx.fieldName, ctx.argName + ".get()") + ";"));
    public static final Type BOOLEAN = new FixedSizedType(1, "BOOLEAN" ,b -> b.get() == 1,
        (ctx) -> ctx.resName + ".writeBoolean(" + getterForBoolean(ctx.fieldName) + ");",
        ((ctx) -> ctx.resName + "." + setterFor(ctx.fieldName, ctx.argName + ".get() == 1") + ";"));

    public static final Type SHORT = new JavaType(2, "SHORT" ,ByteBuffer::getShort);

    public static final Type INTEGER = new JavaType(4, "INT" ,ByteBuffer::getInt);

    public static final Type LONG = new JavaType(8, "LONG" ,ByteBuffer::getLong);
    public static final Type DOUBLE = new JavaType(8, "DOUBLE" ,ByteBuffer::getDouble);
    public static final Type FLOAT = new JavaType(4, "FLOAT" ,ByteBuffer::getFloat);

    public static final Type[] values = new Type[] {
        EMPTY, BYTE, BOOLEAN, SHORT, INTEGER, LONG, DOUBLE, FLOAT
    };

}
