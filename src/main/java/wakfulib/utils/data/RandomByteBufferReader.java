package wakfulib.utils.data;


import wakfulib.utils.StringUtils;
import wakfulib.utils.random.Randomizer;

import java.nio.ByteBuffer;

/**
 * A {@link ByteBuffer} wrapper that applies a rolling seed-based transformation 
 * to all read operations. 
 * This is used for decoding data that has been obfuscated or lightly encrypted 
 * with a dynamic seed.
 */
public class RandomByteBufferReader extends Randomizer {
    private final ByteBuffer m_buffer;

    /**
     * Creates a new reader for the provided buffer.
     *
     * @param buffer the buffer to read from
     * @param mult the multiplier for the seed increment logic
     * @param add the addend for the seed increment logic
     */
    public RandomByteBufferReader(ByteBuffer buffer, int mult, int add) {
        super(mult, add);
        this.m_buffer = buffer;
    }

    @Override
    public long position() {
        return (long)this.m_buffer.position();
    }

    /**
     * Resets the buffer position and the obfuscation seed.
     *
     * @param position the new buffer position
     * @param seed the new starting seed
     */
    public void position(int position, byte seed) {
        this.m_seed = seed;
        this.m_buffer.position(position);
    }

    /**
     * Reads a single byte and de-obfuscates it using the current seed.
     *
     * @return the de-obfuscated byte
     */
    public byte get() {
        this.inc();
        return (byte)(this.m_buffer.get() - this.m_seed);
    }

    public boolean readBoolean() {
        this.inc();
        return this.m_buffer.get() - this.m_seed != 0;
    }

    public short getShort() {
        this.inc();
        return (short)(this.m_buffer.getShort() - this.m_seed);
    }

    public float getFloat() {
        this.inc();
        return this.m_buffer.getFloat();
    }

    public int getInt() {
        this.inc();
        return this.m_buffer.getInt() - this.m_seed;
    }

    public double getDouble() {
        this.inc();
        return this.m_buffer.getDouble();
    }

    public long getLong() {
        this.inc();
        return this.m_buffer.getLong() - (long)this.m_seed;
    }

    public String readUTF8() {
        int size = this.getInt();
        byte[] data = new byte[size];
        this.m_buffer.get(data);
        return StringUtils.fromUTF8(data);
    }

    public byte[] readByteArray() {
        int size = this.getInt();
        byte[] data = new byte[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.get();
        }

        return data;
    }

    public int[] readIntArray() {
        int size = this.getInt();
        int[] data = new int[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.getInt();
        }

        return data;
    }

    public short[] readShortArray() {
        int size = this.getInt();
        short[] data = new short[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.getShort();
        }

        return data;
    }

    public float[] readFloatArray() {
        int size = this.getInt();
        float[] data = new float[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.getFloat();
        }

        return data;
    }

    public String[] readStringArray() {
        int size = this.getInt();
        String[] data = new String[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.readUTF8();
        }

        return data;
    }

    public long[] readLongArray() {
        int size = this.getInt();
        long[] data = new long[size];

        for(int i = 0; i < size; ++i) {
            data[i] = this.getLong();
        }

        return data;
    }
}
