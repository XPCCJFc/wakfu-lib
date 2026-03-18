package wakfulib.utils.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A compact bit set implementation backed by a byte array.
 * Provides bit-level manipulation (get/set) with minimal memory overhead.
 */
public class ByteArrayBitSet {
    private static final int BITS_PER_UNIT = 8;
    private static final int BITS_PER_UNIT_DIVISION = 3;
    private byte[] m_bits;

    private ByteArrayBitSet() {
    }

    /**
     * Copy constructor.
     *
     * @param byteArrayBitSet the bit set to copy
     */
    public ByteArrayBitSet(ByteArrayBitSet byteArrayBitSet) {
        this.m_bits = new byte[byteArrayBitSet.m_bits.length];
        System.arraycopy(byteArrayBitSet.m_bits, 0, this.m_bits, 0, this.m_bits.length);
    }

    /**
     * Creates a bit set of the specified capacity (in bits).
     *
     * @param size number of bits
     */
    public ByteArrayBitSet(int size) {
        this.m_bits = new byte[getDataLength(size)];
    }

    /**
     * Creates a bit set with initial values.
     *
     * @param size number of bits
     * @param defaultValue initial value for all bits
     */
    public ByteArrayBitSet(int size, boolean defaultValue) {
        this.m_bits = new byte[getDataLength(size)];
        this.setAll(defaultValue);
    }

    /**
     * Returns the bit value at the specified index.
     *
     * @param index the bit index
     * @return the bit value
     */
    public final boolean get(int index) {
        return get(this.m_bits, index);
    }

    /**
     * Sets the bit value at the specified index.
     *
     * @param index the bit index
     * @param value the new bit value
     */
    public final void set(int index, boolean value) {
        set(this.m_bits, index, value);
    }

    /**
     * Sets all bits in the set to the specified value.
     *
     * @param value the value to set for all bits
     */
    public final void setAll(boolean value) {
        int i;
        if (value) {
            for(i = 0; i < this.m_bits.length; ++i) {
                this.m_bits[i] = -1;
            }
        } else {
            for(i = 0; i < this.m_bits.length; ++i) {
                this.m_bits[i] = 0;
            }
        }

    }

    private void resize(int newSize) {
        assert newSize >= this.m_bits.length * BITS_PER_UNIT : "loosing data in BitSet (oldSize=" + this.m_bits.length + " newSize=" + newSize + ")";

        byte[] newBits = new byte[(newSize + 7) / BITS_PER_UNIT];
        System.arraycopy(this.m_bits, 0, newBits, 0, this.m_bits.length);
        this.m_bits = newBits;
    }

    public final int capacity() {
        return this.m_bits.length * BITS_PER_UNIT;
    }

    private static byte bit(int index) {
        assert index < BITS_PER_UNIT : "bit index should be < 8 , found : " + index;

        return (byte)(1 << index);
    }

    public final byte[] getByteArray() {
        return this.m_bits;
    }

    public final void write(OutputStream outputStream) throws IOException {
        outputStream.write(this.m_bits);
    }

    public static ByteArrayBitSet fromByteArray(byte[] array, int offset, int size) {
        ByteArrayBitSet bitSet = new ByteArrayBitSet();
        bitSet.m_bits = new byte[size];
        System.arraycopy(array, offset, bitSet.m_bits, 0, size);
        return bitSet;
    }

    public static ByteArrayBitSet wrap(byte[] array) {
        ByteArrayBitSet bitSet = new ByteArrayBitSet();
        bitSet.m_bits = array;
        return bitSet;
    }

    public static boolean get(byte[] bits, int index) {
        assert index >> BITS_PER_UNIT_DIVISION < bits.length : "trying to get a bit index=" + index + " but only " + bits.length * BITS_PER_UNIT + " available.";

        int unitPosition = index >> BITS_PER_UNIT_DIVISION;
        int bitPosition = 7 - (index - (unitPosition << BITS_PER_UNIT_DIVISION));
        return (bits[unitPosition] & bit(bitPosition)) != 0;
    }

    public static void set(byte[] bits, int index, boolean value) {
        assert index >> BITS_PER_UNIT_DIVISION < bits.length : "trying to set a bit index=" + index + " but only " + bits.length * BITS_PER_UNIT + " available.";

        int unitPosition = index >> BITS_PER_UNIT_DIVISION;
        int bitPosition = 7 - (index - (unitPosition << BITS_PER_UNIT_DIVISION));
        if (value) {
            bits[unitPosition] |= bit(bitPosition);
        } else {
            bits[unitPosition] = (byte)(bits[unitPosition] & ~bit(bitPosition));
        }

    }

    public static int getDataLength(int size) {
        return size + 7 >> BITS_PER_UNIT_DIVISION;
    }
}
