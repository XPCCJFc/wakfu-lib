package wakfulib.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for mathematical operations and bit manipulation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathHelper {

    /**
     * Clamps a long value between -128 and 127 and casts it to a byte.
     *
     * @param value the value to clamp and cast
     * @return the clamped byte value
     */
    public static byte ensureByte(long value) {
        return (byte)((int)clamp(value, -128L, 127L));
    }

    /**
     * Clamps a long value between a minimum and maximum range.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     */
    public static long clamp(long value, long min, long max) {
        return value <= min ? min : (Math.min(value, max));
    }

    /**
     * Calculates the exponent {@code c} such that {@code 2^c >= value}.
     * This is effectively a base-2 logarithm rounded up.
     *
     * @param value the positive integer to calculate for
     * @return the smallest integer {@code c} where {@code 2^c >= value}
     */
    public static int log2i(int value) {
        assert value > 0;
        int count = 1;
        int c;
        for (c = 0; value > count; ++c) {
            count *= 2;
        }

        return c;
    }

    /**
     * Extracts the most significant 16 bits (first short) from a 32-bit integer.
     *
     * @param value the source integer
     * @return the high-order short
     */
    public static short getFirstShortFromInt(int value) {
        return (short)(value >> 16 & 0xFFFF);
    }

    /**
     * Extracts the least significant 16 bits (second short) from a 32-bit integer.
     *
     * @param value the source integer
     * @return the low-order short
     */
    public static short getSecondShortFromInt(int value) {
        return (short)(value & 0xFFFF);
    }

    /**
     * Combines two 16-bit shorts into a single 32-bit integer.
     *
     * @param a the high-order short
     * @param b the low-order short
     * @return the combined integer
     */
    public static int getIntFromTwoShort(short a, short b) {
        int la = a & 0xFFFF;
        int lb = b & 0xFFFF;
        return la << 16 | lb;
    }
}
