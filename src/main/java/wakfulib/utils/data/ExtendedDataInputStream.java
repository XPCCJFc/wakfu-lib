package wakfulib.utils.data;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import wakfulib.utils.StringUtils;

/**
 * An enhanced input stream for reading various primitive types and structured data 
 * from a {@link ByteBuffer}. 
 * It supports common Wakfu-specific patterns like packed boolean bit fields 
 * and null-terminated UTF-8 strings.
 */
public class ExtendedDataInputStream implements AutoCloseable {
    public static final ByteOrder DEFAULT_ORDERING;
    private final ByteBuffer m_byteBuffer;
    private int m_lastBooleanBitFieldPosition = -1;
    private byte m_lastBooleanBitFieldIndex = -1;
    private byte m_lastBooleanBitFieldValue = 0;
    private static final ByteBuffer EMPTY_BYTE_BUFFER;
    private byte[] stringBuffer = new byte[128];

    protected ExtendedDataInputStream(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            throw new IllegalArgumentException("ByteBuffer can't be null");
        } else {
            this.m_byteBuffer = byteBuffer;
            this.m_byteBuffer.order(DEFAULT_ORDERING);
        }
    }

    /**
     * Reads all available bytes from an {@link InputStream} and wraps it in an {@link ExtendedDataInputStream}.
     * The input stream is closed after reading.
     *
     * @param stream the stream to read and wrap
     * @return a new ExtendedDataInputStream containing the full stream content
     * @throws IOException if an I/O error occurs
     */
    public static ExtendedDataInputStream wrapFullAndClose(InputStream stream) throws IOException {
        ExtendedDataInputStream result = new ExtendedDataInputStream(readFullStream(stream));
        stream.close();
        return result;
    }

    /**
     * Wraps a {@link ByteBuffer} in an {@link ExtendedDataInputStream}.
     *
     * @param byteBuffer the buffer to wrap
     * @return a new ExtendedDataInputStream instance
     */
    public static ExtendedDataInputStream wrap(ByteBuffer byteBuffer) {
        return new ExtendedDataInputStream(byteBuffer);
    }

    /**
     * Wraps a {@link ByteBuffer} with a specific byte order.
     *
     * @param byteBuffer the buffer to wrap
     * @param ordering the byte order to apply to the buffer
     * @return a new ExtendedDataInputStream instance
     */
    public static ExtendedDataInputStream wrap(ByteBuffer byteBuffer, ByteOrder ordering) {
        byteBuffer.order(ordering);
        return new ExtendedDataInputStream(byteBuffer);
    }

    /**
     * Wraps a byte array in an {@link ExtendedDataInputStream}.
     *
     * @param data the byte array to wrap
     * @return a new ExtendedDataInputStream instance
     */
    public static ExtendedDataInputStream wrap(byte[] data) {
        return new ExtendedDataInputStream(ByteBuffer.wrap(data));
    }

    public static ExtendedDataInputStream wrap(byte[] data, ByteOrder ordering) {
        return new ExtendedDataInputStream(ByteBuffer.wrap(data).order(ordering));
    }

    protected static ByteBuffer readFullStream(InputStream stream) throws IOException {
        byte[] dataBuffer = null;

        while(stream.available() != 0) {
            int bufferSize = stream.available();
            byte[] data = new byte[bufferSize];

            int dataRead;
            for(int bytesInBuffer = 0; bytesInBuffer != bufferSize; bytesInBuffer += dataRead) {
                dataRead = stream.read(data, bytesInBuffer, bufferSize - bytesInBuffer);
                if (dataRead == -1) {
                    throw new EOFException("Less data than assumed in the stream. " + bufferSize + " expected, " + bytesInBuffer + " read");
                }
            }

            if (dataBuffer == null) {
                dataBuffer = data;
            } else {
                byte[] tmp = new byte[dataBuffer.length + data.length];
                System.arraycopy(dataBuffer, 0, tmp, 0, dataBuffer.length);
                System.arraycopy(data, 0, tmp, dataBuffer.length, data.length);
                dataBuffer = tmp;
            }
        }

        if (dataBuffer != null) {
            return ByteBuffer.wrap(dataBuffer);
        } else {
            return ByteBuffer.allocate(0);
        }
    }

    public final void order(ByteOrder order) {
        this.m_byteBuffer.order(order);
    }

    public final ByteOrder order() {
        return this.m_byteBuffer.order();
    }

    public final int skip(int n) {
        if (n <= 0) {
            return 0;
        } else {
            int remaining = this.m_byteBuffer.remaining();
            int bytesToSkip = Math.min(remaining, n);
            this.m_byteBuffer.position(this.m_byteBuffer.position() + bytesToSkip);
            return bytesToSkip;
        }
    }

    public final int available() {
        return this.m_byteBuffer.remaining();
    }

    public void close() {
    }

    public final int readBytes(byte[] b, int offset, int size) {
        int bytesToRead = Math.min(this.available(), Math.min(b.length - offset, size));
        this.m_byteBuffer.get(b, offset, bytesToRead);
        return bytesToRead;
    }

    public final int readBytes(byte[] b) {
        int bytesToRead = Math.min(this.available(), b.length);
        this.m_byteBuffer.get(b, 0, bytesToRead);
        return bytesToRead;
    }

    public final byte[] readBytes(int length) {
        byte[] result = new byte[length];
        this.m_byteBuffer.get(result);
        return result;
    }

    public final short[] readShorts(int length) {
        short[] result = new short[length];

        for(int i = 0; i < length; ++i) {
            result[i] = this.m_byteBuffer.getShort();
        }

        return result;
    }

    public final int[] readInts(int length) {
        int[] result = new int[length];

        for(int i = 0; i < length; ++i) {
            result[i] = this.m_byteBuffer.getInt();
        }

        return result;
    }

    public final float[] readFloats(int length) {
        float[] result = new float[length];

        for(int i = 0; i < length; ++i) {
            result[i] = this.m_byteBuffer.getFloat();
        }

        return result;
    }

    public final float readFloat() {
        return this.m_byteBuffer.getFloat();
    }

    public final short readShort() {
        return this.m_byteBuffer.getShort();
    }

    public final int readUnsignedShort() {
        return this.m_byteBuffer.getShort() & '\uffff';
    }

    public final int readInt() {
        return this.m_byteBuffer.getInt();
    }

    public final long readUnsignedInt() {
        return (long)this.m_byteBuffer.getInt() & 4294967295L;
    }

    public final long readLong() {
        return this.m_byteBuffer.getLong();
    }

    public final byte readByte() {
        return this.m_byteBuffer.get();
    }

    public final short readUnsignedByte() {
        return (short)((short)this.m_byteBuffer.get() & 255);
    }

    /**
     * Reads a single bit from the stream as a boolean.
     * Successive calls will read subsequent bits from the same byte until all 8 bits are consumed,
     * at which point a new byte is read from the underlying buffer.
     *
     * @return the bit value as a boolean
     */
    public final boolean readBooleanBit() {
        int currentPosition = this.m_byteBuffer.position();
        if (currentPosition == this.m_lastBooleanBitFieldPosition && this.m_lastBooleanBitFieldIndex <= 6) {
            ++this.m_lastBooleanBitFieldIndex;
            return (this.m_lastBooleanBitFieldValue & 1 << 7 - this.m_lastBooleanBitFieldIndex) != 0;
        } else {
            this.m_lastBooleanBitFieldIndex = 0;
            this.m_lastBooleanBitFieldPosition = currentPosition + 1;
            this.m_lastBooleanBitFieldValue = this.m_byteBuffer.get();
            int i = this.m_lastBooleanBitFieldValue & 128;
            return i != 0;
        }
    }

    /**
     * Reads a null-terminated UTF-8 string from the stream.
     *
     * @return the decoded string
     * @throws EOFException if the end of the buffer is reached before finding a null terminator
     */
    public final String readString() throws EOFException {
        int limit = this.m_byteBuffer.limit();

        int i;
        i = this.m_byteBuffer.position();
        while (i < limit && this.m_byteBuffer.get(i) != 0) {
            i = i + 1;
        }

        if (i >= limit) {
            throw new EOFException("Unable to find a valid Null terminated UTF-8 string end.");
        } else {
            int length = i - this.m_byteBuffer.position();
            if (length > 0) {
                if (length > this.stringBuffer.length) {
                    this.stringBuffer = new byte[length];
                }

                this.m_byteBuffer.get(this.stringBuffer, 0, length);
                this.m_byteBuffer.get();
                return StringUtils.fromUTF8(this.stringBuffer, 0, length);
            } else {
                this.m_byteBuffer.get();
                return "";
            }
        }
    }

    public final int getOffset() {
        return this.m_byteBuffer.position();
    }

    public final void setOffset(int offset) {
        this.m_byteBuffer.position(offset);
    }

    static {
        DEFAULT_ORDERING = ByteOrder.LITTLE_ENDIAN;
        EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
    }
}
