package wakfulib.utils.random;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for common random-related operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomUtils {

    private static RandomProvider RANDOM_PROVIDER = MersenneTwister.getInstance();

    /**
     * Configures the random number provider.
     *
     * @param randomProvider the provider implementation
     */
    public static void setRandomProvider(RandomProvider randomProvider) {
        RANDOM_PROVIDER = randomProvider;
    }

    /**
     * Performs probabilistic rounding of a float value. 
     * For example, a value of 1.2 has a 20% chance of being rounded to 2 
     * and an 80% chance of being rounded to 1.
     *
     * @param value the float value to round
     * @return the probabilistically rounded integer
     */
    public static int randomRound(float value) {
        double integerPart = Math.floor(value);
        double decimalPart = (double)value - integerPart;
        if (RANDOM_PROVIDER.nextBoolean(decimalPart)) {
            integerPart += 1.0;
        }
        return (int)integerPart;
    }
}
