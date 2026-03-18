package wakfulib.utils.data;

import lombok.Data;

/**
 * A simple container for a pair of related objects.
 *
 * @param <T> type of the first element
 * @param <U> type of the second element
 */
@Data
public class Tuple<T, U> {
    /** The first element. */
    public final T _1;
    /** The second element. */
    public final U _2;
}
