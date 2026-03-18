package wakfulib.utils;

import lombok.RequiredArgsConstructor;

/**
 * Utility class for managing text indentation.
 * Useful for generating formatted output like code, logs, or structured data.
 */
@RequiredArgsConstructor
public final class Tabulator {
    private int lvl = 0;
    private final String tabSequence;

    /**
     * Returns a string consisting of the repeated tab sequence based on the current level.
     *
     * @return the indentation string
     */
    public String tab() {
        return tabSequence.repeat(lvl);
    }

    /**
     * Increments the indentation level.
     */
    public void increment() {
        lvl++;
    }

    /**
     * Decrements the indentation level. Will not go below zero.
     */
    public void decrement() {
        if (lvl == 0) return;
        lvl--;
    }
}
