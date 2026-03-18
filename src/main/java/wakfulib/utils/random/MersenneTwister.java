package wakfulib.utils.random;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * An implementation of the Mersenne Twister pseudorandom number generator (PRNG).
 * The Mersenne Twister is a widely used PRNG that offers a very long period (2^19937-1)
 * and high statistical quality compared to {@link java.util.Random}.
 * It is suitable for simulations and gaming applications requiring high-quality randomness.
 */
public class MersenneTwister extends Random implements Serializable, RandomProvider {
    private static final long serialVersionUID = 2932129847991607657L;
    private static final MersenneTwister _instance = new MersenneTwister();
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = -1727483681;
    private static final int UPPER_MASK = Integer.MIN_VALUE;
    private static final int LOWER_MASK = Integer.MAX_VALUE;
    private static final int TEMPERING_MASK_B = -1658038656;
    private static final int TEMPERING_MASK_C = -272236544;
    private int[] mt;
    private int mti;
    private int[] mag01;
    private double __nextNextGaussian;
    private boolean __haveNextNextGaussian;

    public MersenneTwister() {
        this(System.currentTimeMillis());
    }

    public MersenneTwister(long seed) {
        super(seed);
        this.setSeed(seed);
    }

    public MersenneTwister(int[] array) {
        super(System.currentTimeMillis());
        this.setSeed(array);
    }

    public synchronized void setSeed(long seed) {
        super.setSeed(seed);
        this.__haveNextNextGaussian = false;
        this.mt = new int[624];
        this.mag01 = new int[2];
        this.mag01[1] = -1727483681;
        this.mt[0] = (int)(seed & 268435455L);

        for(this.mti = 1; this.mti < 624; ++this.mti) {
            this.mt[this.mti] = 1812433253 * (this.mt[this.mti - 1] ^ this.mt[this.mti - 1] >>> 30) + this.mti;
            int[] var10000 = this.mt;
            int var10001 = this.mti;
            var10000[var10001] &= -1;
        }

    }

    public synchronized void setSeed(int[] array) {
        this.setSeed(19650218L);
        int i = 1;
        int j = 0;

        int[] var10000;
        int k;
        for(k = 624 > array.length ? 624 : array.length; k != 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1664525) + array[j] + j;
            var10000 = this.mt;
            var10000[i] &= -1;
            ++i;
            ++j;
            if (i >= 624) {
                this.mt[0] = this.mt[623];
                i = 1;
            }

            if (j >= array.length) {
                j = 0;
            }
        }

        for(k = 623; k != 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1566083941) - i;
            var10000 = this.mt;
            var10000[i] &= -1;
            ++i;
            if (i >= 624) {
                this.mt[0] = this.mt[623];
                i = 1;
            }
        }

        this.mt[0] = Integer.MIN_VALUE;
    }

    protected synchronized int next(int bits) {
        int y;
        if (this.mti >= 624) {
            int kk;
            for(kk = 0; kk < 227; ++kk) {
                y = this.mt[kk] & Integer.MIN_VALUE | this.mt[kk + 1] & Integer.MAX_VALUE;
                this.mt[kk] = this.mt[kk + 397] ^ y >>> 1 ^ this.mag01[y & 1];
            }

            while(kk < 623) {
                y = this.mt[kk] & Integer.MIN_VALUE | this.mt[kk + 1] & Integer.MAX_VALUE;
                this.mt[kk] = this.mt[kk - 227] ^ y >>> 1 ^ this.mag01[y & 1];
                ++kk;
            }

            y = this.mt[623] & Integer.MIN_VALUE | this.mt[0] & Integer.MAX_VALUE;
            this.mt[623] = this.mt[396] ^ y >>> 1 ^ this.mag01[y & 1];
            this.mti = 0;
        }

        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & -1658038656;
        y ^= y << 15 & -272236544;
        y ^= y >>> 18;
        return y >>> 32 - bits;
    }

    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public boolean nextBoolean() {
        return this.next(1) != 0;
    }

    public boolean nextBoolean(float probability) {
        if (probability >= 0.0F && probability <= 1.0F) {
            if (probability == 0.0F) {
                return false;
            } else if (probability == 1.0F) {
                return true;
            } else {
                return this.nextFloat() < probability;
            }
        } else {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
    }

    public boolean nextBoolean(double probability) {
        if (probability >= 0.0D && probability <= 1.0D) {
            if (probability == 0.0D) {
                return false;
            } else if (probability == 1.0D) {
                return true;
            } else {
                return this.nextDouble() < probability;
            }
        } else {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
    }

    public int nextInt(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        } else if (n == 0) {
            return 0;
        } else if ((n & -n) == n) {
            return (int)((long)n * (long)this.next(31) >> 31);
        } else {
            int bits;
            int val;
            do {
                bits = this.next(31);
                val = bits % n;
            } while(bits - val + (n - 1) < 0);

            return val;
        }
    }

    public long nextLong(long n) {
        if (n < 0L) {
            throw new IllegalArgumentException("n must be > 0");
        } else if (n == 0L) {
            return 0L;
        } else {
            long bits;
            long val;
            do {
                bits = this.nextLong() >>> 1;
                val = bits % n;
            } while(bits - val + (n - 1L) < 0L);

            return val;
        }
    }

    public double nextDouble() {
        return (double)(((long)this.next(26) << 27) + (long)this.next(27)) / 9.007199254740992E15D;
    }

    public float nextFloat() {
        return (float)this.next(24) / 1.6777216E7F;
    }

    public void nextBytes(byte[] bytes) {
        for(int x = 0; x < bytes.length; ++x) {
            bytes[x] = (byte)this.next(8);
        }

    }

    public char nextChar() {
        return (char)this.next(16);
    }

    public short nextShort() {
        return (short)this.next(16);
    }

    public byte nextByte() {
        return (byte)this.next(8);
    }

    public synchronized double nextGaussian() {
        if (this.__haveNextNextGaussian) {
            this.__haveNextNextGaussian = false;
            return this.__nextNextGaussian;
        } else {
            double v1;
            double v2;
            double s;
            do {
                do {
                    v1 = 2.0D * this.nextDouble() - 1.0D;
                    v2 = 2.0D * this.nextDouble() - 1.0D;
                    s = v1 * v1 + v2 * v2;
                } while(s >= 1.0D);
            } while(s == 0.0D);

            double multiplier = Math.sqrt(-2.0D * Math.log(s) / s);
            this.__nextNextGaussian = v2 * multiplier;
            this.__haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    public static synchronized MersenneTwister getInstance() {
        return _instance;
    }
}
