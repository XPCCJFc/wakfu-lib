package wakfulib.utils;

import java.lang.reflect.Array;
import java.util.function.IntFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for array-related operations, including generic array creation
 * and basic searching.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayUtils {
    /**
     * Creates an {@link IntFunction} that generates arrays of a generic type R.
     * This is useful when you need to create arrays of a generic type that is a subclass of T.
     *
     * @param <T> the base type
     * @param <R> the specialized type
     * @param arrayCreator a function that creates an array of type T
     * @return a function that creates an array of type R
     */
    @SuppressWarnings("unchecked")
    public static <T, R extends T> IntFunction<R[]> genericArray(IntFunction<T[]> arrayCreator) {
        return size -> (R[]) arrayCreator.apply(size);
    }

    /**
     * Creates a new array of the specified class and capacity.
     *
     * @param <T> the component type of the array
     * @param clazz the class representing the component type
     * @param capacity the size of the array to create
     * @return a new array of type T with the given capacity
     */
    public static <T> T[] genericArray(Class<T> clazz, int capacity) {
        return (T[]) Array.newInstance(clazz, capacity);
    }

    /**
     * Checks if the specified integer array contains a specific value.
     *
     * @param array the array to search in
     * @param search the value to search for
     * @return {@code true} if the value is found, {@code false} otherwise
     */
    public static boolean contains(int[] array, int search) {
        for (int j : array) {
            if (j == search) {
                return true;
            }
        }
        return false;
    }
}
