package wakfulib.utils.random;

/**
 * Base class for data obfuscation or light encryption based on a rolling seed.
 * The seed is updated after each operation based on the current position 
 * and fixed multipliers/addends.
 */
public abstract class Randomizer {
    protected byte m_seed;
    private final int m_add;
    private final int m_mult;

    /**
     * Initializes the randomizer with the specified parameters.
     *
     * @param mult the multiplier for the rolling seed logic
     * @param add the addend for the rolling seed logic
     */
    protected Randomizer(int mult, int add) {
        this.m_mult = mult;
        this.m_add = add;
        this.m_seed = (byte)(this.m_mult ^ add);
    }

    /**
     * Increments the rolling seed based on the current position.
     */
    protected final void inc() {
        this.m_seed = (byte)((int)((long)this.m_seed + (long)this.m_mult * this.position() + (long)this.m_add));
    }

    /**
     * Gets the current position used for seed calculation.
     *
     * @return the current position
     */
    protected abstract long position();
}
