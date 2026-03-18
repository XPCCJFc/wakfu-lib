package wakfulib.ui.proxy.model.def.type.storage;

import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.getterFor;
import static wakfulib.ui.proxy.model.def.scripting.generation.InstructionContextHelper.setterFor;

import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter;
import wakfulib.ui.proxy.model.def.type.SizeVariableType;
import wakfulib.utils.StringUtils;

public final class ComplexeTypes {

    public static final SizeVariableType POP_STRING = new SizeVariableType("pSTRING", (a, b, s, args) -> {
        final int size = ((Number) s.pop()).intValue();
        final byte[] res = new byte[size];
        b.get(res);
        a.addAndGet(size);
        return StringUtils.fromUTF8(res);
    }, "$POPBUFFER");

    public static final SizeVariableType POP_BUFFER = new SizeVariableType("pBUFFER", (a, b, s, args) -> {
        final int size = ((Number) s.pop()).intValue();
        final byte[] res = new byte[size];
        b.get(res);
        a.addAndGet(size);
        return res;
    }, "$POPBUFFER");

    public static final SizeVariableType BYTE_STRING = new SizeVariableType("bSTRING", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.get();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 1);
        return StringUtils.fromUTF8(res);
    }, stringReaderFactory(ctx -> "(int) " + ctx.argName + ".get()"),
        ctx -> ctx.resName + ".writeByteStringUTF8(" + getterFor(ctx.fieldName) + ");", "String");

    public static final SizeVariableType SHORT_STRING = new SizeVariableType("sSTRING", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.getShort();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 2);
        return StringUtils.fromUTF8(res);
    }, stringReaderFactory(ctx -> "(int) " + ctx.argName + ".getShort()"),
        ctx -> ctx.resName + ".writeShortStringUTF8(" + getterFor(ctx.fieldName) + ");", "String");

    public static final SizeVariableType C_STRING = new SizeVariableType("cSTRING", (atomicInteger, byteBuffer, s, args) -> {
        var saveOffset = byteBuffer.position();
        while ((byteBuffer.get()) != 0) {}
        var size = byteBuffer.position() - saveOffset - 1;
        final byte[] res = new byte[size];
        byteBuffer.position(saveOffset);
        byteBuffer.get(res, 0, size);
        if (byteBuffer.get() != 0) {
            throw new IllegalStateException("End of string should have been a 0 !");
        }
        atomicInteger.addAndGet(size + 1);
        return StringUtils.fromUTF8(res);
    }, stringReaderFactory(ctx -> "(int) " + ctx.argName + ".getShort()"),
        ctx -> ctx.resName + ".writeString(" + getterFor(ctx.fieldName) + ");", "String");

    public static final SizeVariableType INT_STRING = new SizeVariableType("iSTRING", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.getInt();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 4);
        return StringUtils.fromUTF8(res);
    }, stringReaderFactory(ctx -> ctx.argName + ".getInt()"),
        ctx -> ctx.resName + ".writeIntStringUTF8(" + getterFor(ctx.fieldName) + ");", "String");

    public static final SizeVariableType BYTE_BUFFER = new SizeVariableType("bBUFFER", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.get();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 4);
        return res;
    }, bufferReaderFactory(ctx -> "(int) " + ctx.argName + ".get()"),
        bufferWriterFactory(ctx -> "writeByte(" + getterFor(ctx.fieldName) + ".length)"), "byte[]");

    public static final SizeVariableType INT_BUFFER = new SizeVariableType("iBUFFER", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.getInt();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 4);
        return res;
    }, bufferReaderFactory(ctx -> ctx.argName + ".getInt()"),
    bufferWriterFactory(ctx -> "writeInt(" + getterFor(ctx.fieldName) + ".length)"), "byte[]");

    public static final SizeVariableType SHORT_BUFFER = new SizeVariableType("sBUFFER", (atomicInteger, byteBuffer, s, args) -> {
        final short size = byteBuffer.getShort();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size + 2);
        return res;
    }, bufferReaderFactory(ctx -> "(int) " + ctx.argName + ".getShort()"),
        bufferWriterFactory(ctx -> "writeShort(" + getterFor(ctx.fieldName) + ".length)"), "byte[]");

    public static final SizeVariableType REMAINING_BUFFER = new SizeVariableType("rBuffer", (atomicInteger, byteBuffer, s, args) -> {
        final int size = byteBuffer.remaining();
        final byte[] res = new byte[size];
        byteBuffer.get(res);
        atomicInteger.addAndGet(size);
        return res;
    }, bufferReaderFactory(ctx -> ctx.argName + ".remaining()"),
        ctx -> ctx.resName + ".writeBytes(" + getterFor(ctx.fieldName) + ");",
        "byte[]");

    public static final SizeVariableType REMAINING_STRING  = new SizeVariableType("rString", (atomicInteger, byteBuffer, s, args) -> {
      final int size = byteBuffer.remaining();
      final byte[] res = new byte[size];
      byteBuffer.get(res);
      atomicInteger.addAndGet(size);
      return StringUtils.fromUTF8(res);
    }, stringReaderFactory(ctx -> ctx.argName + ".remaining()"),
        ctx -> {
        ctx.additionalImports.add("import wakfulib.utils.StringUtils;");
        return ctx.resName + ".writeBytes(StringUtils.toUTF8(" + getterFor(ctx.fieldName) + "));";
        },
        "String");

    public static final SizeVariableType[] values = new SizeVariableType[] {
        BYTE_STRING, SHORT_STRING, INT_STRING, C_STRING,
        REMAINING_STRING, POP_STRING, POP_BUFFER, BYTE_BUFFER, SHORT_BUFFER, INT_BUFFER,
        REMAINING_BUFFER
    };

  private static InstructionWriter stringReaderFactory(InstructionWriter getSize) {
        return ctx -> {
            ctx.additionalImports.add("import wakfulib.utils.StringUtils;");
            String bufferName = ctx.fieldName + "Buffer";
            return "final byte[] " + bufferName + " = new byte[" + getSize.apply(ctx) + "];\n"
                + ctx.argName + ".get(" + bufferName + ");\n"
                + ctx.resName + "." + setterFor(ctx.fieldName, "StringUtils.fromUTF8(" + bufferName + ")" ) + ";";
        };
    }

    private static InstructionWriter bufferReaderFactory(InstructionWriter getSize) {
        return ctx -> {
            String bufferName = ctx.fieldName + "Buffer";
            return "final byte[] " + bufferName + " = new byte[" + getSize.apply(ctx) + "];\n"
                + ctx.argName + ".get(" + bufferName + ");\n"
                + ctx.resName + "." + setterFor(ctx.fieldName,bufferName) + ";";
        };
    }

    private static InstructionWriter bufferWriterFactory(InstructionWriter getSize) {
        return ctx -> ctx.resName + "." + getSize.apply(ctx) + ";\n"
            + ctx.resName + ".writeBytes(" + getterFor(ctx.fieldName) + ");";
    }
}
