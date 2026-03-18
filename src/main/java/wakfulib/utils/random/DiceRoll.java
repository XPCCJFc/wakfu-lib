package wakfulib.utils.random;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for simulating dice rolls.
 * Supports standard RPG-style rolls (e.g., 1d6, 3d10 + moderator).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiceRoll {

    private static RandomProvider RANDOM_PROVIDER = MersenneTwister.getInstance();

    /**
     * Configures the random number provider to use for all rolls.
     *
     * @param randomProvider the provider implementation
     */
    public static void setRandomProvider(RandomProvider randomProvider) {
        RANDOM_PROVIDER = randomProvider;
    }

    /**
     * Simulates rolling a single die with the specified number of faces.
     *
     * @param diceValue the number of faces (e.g., 6 for a d6)
     * @return a random value between 1 and diceValue
     */
    public static int roll(int diceValue) {
        if (diceValue <= 0) {
//            m_logger.error("DiceRoll.roll appelé avec une valeur de dé de " + diceValue + "\n" + ExceptionFormatter.toString(new RuntimeException("StackTrace de DiceRoll")));
            return 1;
        } else {
            return RANDOM_PROVIDER.nextInt(diceValue) + 1;
        }
    }

    /**
     * Simulates rolling a single die with the specified number of faces (long).
     *
     * @param diceValue the number of faces
     * @return a random value between 1 and diceValue
     */
    public static long roll(long diceValue) {
        if (diceValue <= 0L) {
//            m_logger.error("DiceRoll.roll appelé avec une valeur de dé de " + diceValue + "\n" + ExceptionFormatter.toString(new RuntimeException("StackTrace de DiceRoll")));
            return 1L;
        } else {
            return RANDOM_PROVIDER.nextLong(diceValue) + 1L;
        }
    }

    /**
     * Simulates rolling multiple dice and adding a moderator.
     *
     * @param diceCount number of dice to roll
     * @param diceValue number of faces on each die
     * @param moderator a value added to the final result
     * @return the total sum of all rolls plus the moderator
     */
    public static int roll(int diceCount, int diceValue, int moderator) {
        if (diceValue <= 0) {
//            m_logger.error("DiceRoll.roll appelé avec une valeur de dé de " + diceValue + "\n" + ExceptionFormatter.toString(new RuntimeException("StackTrace de DiceRoll")));
            return 1;
        } else {
            int total = moderator + diceCount;
            if (diceCount > 0) {
                for(int i = diceCount; i > 0; --i) {
                    total += RANDOM_PROVIDER.nextInt(diceValue);
                }
            }

            return total;
        }
    }

    /**
     * Generates a random value within a specified range [min, max].
     *
     * @param min the minimum inclusive value
     * @param max the maximum inclusive value
     * @return a random value within the range
     */
    public static int roll(int min, int max) {
        int total = min;
        if (min > 0 && max > 0 && max - min > 0) {
            total = min + RANDOM_PROVIDER.nextInt(max - min + 1);
        }

        return total;
    }
}
