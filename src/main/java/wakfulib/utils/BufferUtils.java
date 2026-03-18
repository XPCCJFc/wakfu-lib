package wakfulib.utils;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logic.OutPacket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for common buffer operations, supporting both Netty and NIO buffers.
 * Provides methods for conversions, array extraction, and encoding/decoding of structured data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BufferUtils {
    /**
     * Converts a Netty {@link ByteBuf} to a NIO {@link ByteBuffer}.
     *
     * @param buffer The Netty buffer to convert.
     * @return The corresponding NIO buffer.
     */
    public static ByteBuffer toNioBuffer(@NonNull ByteBuf buffer) {
        return buffer.nioBuffer();
    }

    /**
     * Reads a specific number of bytes from a Netty {@link ByteBuf} and wraps them in a NIO {@link ByteBuffer}.
     *
     * @param buffer The Netty buffer to read from.
     * @param size The number of bytes to read.
     * @return A NIO buffer containing the read bytes.
     */
    public static ByteBuffer toNioBuffer(@NonNull ByteBuf buffer, int size) {
        final byte[] bytes = new byte[size];
        buffer.readBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }

    /**
     * Extracts all readable bytes from a Netty {@link ByteBuf} into a byte array.
     *
     * @param buffer The Netty buffer to extract from.
     * @return A byte array containing the buffer's contents.
     */
    @NonNull
    public static byte[] toArray(@NonNull ByteBuf buffer) {
        var bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    /**
     * Extracts all remaining bytes from a NIO {@link ByteBuffer} into a byte array.
     *
     * @param buffer The NIO buffer to extract from.
     * @return A byte array containing the remaining bytes in the buffer.
     */
    @NonNull
    public static byte[] toArray(@NonNull ByteBuffer buffer) {
        var bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Converts a byte array to a human-readable string (optionally in hex format).
     *
     * @param buf The byte array to convert.
     * @param hex {@code true} to use hexadecimal format, {@code false} for decimal.
     * @return A formatted string representation of the bytes.
     */
    @NonNull
    public static String toString(@NonNull byte[] buf, boolean hex) {
        var sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String h = (hex ? Integer.toHexString(buf[i] & 0xFF).toUpperCase() : String.valueOf(buf[i]));
            if (h.length() == 1) {
                h = "0" + h;
            }
            sb.append(h).append(i == buf.length - 1 ? "" : " ");
            if ((i+1) % 16 == 0) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * Reads a UTF-8 string from a buffer, prefixed by a single-byte length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded string.
     */
    @NonNull
    public static String getByteStringUTF8(@NonNull ByteBuffer byteBuf) {
        byte[] stringBytes = new byte[byteBuf.get()];
        byteBuf.get(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads a UTF-8 string from a buffer, prefixed by a short (2-byte) length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded string.
     */
    @NonNull
    public static String getShortStringUTF8(@NonNull ByteBuffer byteBuf) {
        byte[] stringBytes = new byte[byteBuf.getShort()];
        byteBuf.get(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads a UTF-8 string from a buffer, prefixed by an integer (4-byte) length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded string.
     */
    @NonNull
    public static String getIntStringUTF8(@NonNull ByteBuffer byteBuf) {
        byte[] stringBytes = new byte[byteBuf.getInt()];
        byteBuf.get(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads an array of integers from a buffer, prefixed by an integer length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded integer array.
     */
    @NonNull
    public static int[] getIntSizeIntegerArray(@NonNull ByteBuffer byteBuf) {
        int[] array;
        int arrayLength = byteBuf.getInt();
        try {
            array = new int[arrayLength];
        }
        catch (OutOfMemoryError e) {
            System.err.println("Taille du tableau alloué : " + arrayLength);
            throw e;
        }
        int i = 0;
        while (i < arrayLength) {
            array[i] = byteBuf.getInt();
            ++i;
        }
        return array;
    }

    /**
     * Reads an array of integers from a buffer, prefixed by a single-byte length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded integer array.
     */
    @NonNull
    public static int[] getByteSizeIntegerArray(@NonNull ByteBuffer byteBuf) {
        int[] array;
        int arrayLength = byteBuf.get();
        try {
            array = new int[arrayLength];
        }
        catch (OutOfMemoryError e) {
            System.err.println("Taille du tableau alloué : " + arrayLength);
            throw e;
        }
        int i = 0;
        while (i < arrayLength) {
            array[i] = byteBuf.getInt();
            ++i;
        }
        return array;
    }

    /**
     * Writes an array of integers to a packet, prefixed by its length as a single byte.
     *
     * @param outPacket The packet to write to.
     * @param ints The integer array to write.
     */
    public static void writeByteSizedIntegerArray(@NonNull OutPacket outPacket, @Nullable int[] ints) {
        if (ints == null) {
            outPacket.writeByte(0);
            return;
        }
        outPacket.writeByte(ints.length);
        for (int aFloat : ints) {
            outPacket.writeInt(aFloat);
        }
    }

    /**
     * Writes an array of integers to a packet, prefixed by its length as an integer.
     *
     * @param outPacket The packet to write to.
     * @param ints The integer array to write.
     */
    public static void writeIntSizedIntegerArray(@NonNull OutPacket outPacket, @Nullable int[] ints) {
        if (ints == null) {
            outPacket.writeInt(0);
            return;
        }
        outPacket.writeInt(ints.length);
        for (int aFloat : ints) {
            outPacket.writeInt(aFloat);
        }
    }

    /**
     * Reads an array of longs from a buffer, prefixed by an integer length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded long array.
     */
    @NonNull
    public static long[] getIntSizedLongArray(@NonNull ByteBuffer byteBuf) {
        int arrayLength = byteBuf.getInt();
        long[] array;
        try {
            array = new long[arrayLength];
        }
        catch (OutOfMemoryError e) {
            System.err.println("Taille du tableau alloué : " + arrayLength);
            throw e;
        }
        int i = 0;
        while (i < arrayLength) {
            array[i] = byteBuf.getLong();
            ++i;
        }
        return array;
    }

    /**
     * Writes an array of floats to a packet, prefixed by its length as an integer.
     *
     * @param outPacket The packet to write to.
     * @param floats The float array to write.
     */
    public static void writeIntSizedFloatArray(@NonNull OutPacket outPacket, @Nullable float[] floats) {
        if (floats == null) {
            outPacket.writeInt(0);
            return;
        }
        outPacket.writeInt(floats.length);
        for (float aFloat : floats) {
            outPacket.writeFloat(aFloat);
        }
    }

    /**
     * Reads an array of floats from a buffer, prefixed by an integer length.
     *
     * @param byteBuf The buffer to read from.
     * @return The decoded float array.
     */
    @NonNull
    public static float[] getIntSizedFloatArray(@NonNull ByteBuffer byteBuf) {
        int arrayLength = byteBuf.getInt();
        float[] array;
        try {
            array = new float[arrayLength];
        }
        catch (OutOfMemoryError e) {
            System.err.println("Taille du tableau alloué : " + arrayLength);
            throw e;
        }
        int i = 0;
        while (i < arrayLength) {
            array[i] = byteBuf.getFloat();
            ++i;
        }
        return array;
    }

    /**
     * Checks if the buffer still has remaining bytes and throws an exception if it does.
     * Used for validating that a packet has been fully consumed.
     *
     * @param byteBuf The buffer to check.
     * @throws IllegalStateException If the buffer has remaining bytes.
     */
    public static void errorIfRemaining(@NonNull ByteBuffer byteBuf) {
        if (byteBuf.hasRemaining()) {
            throw new IllegalStateException("Still " + byteBuf.remaining() + " left :(");
        }
    }

}
