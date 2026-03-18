package wakfulib.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;

/**
 * Utility class for reflection-based operations.
 * Provides methods for accessing and modifying internal fields, including final ones,
 * and a system for automatic type conversion during field assignment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * A setter provider that performs no type conversion.
     */
    public static final SetterProvider NO_CAST_REFLECT = (f, p) -> ((v) -> f.set(p, v));

    /**
     * A setter provider that automatically converts String values to the appropriate 
     * primitive or wrapper type of the target field.
     */
    public static final SetterProvider FROM_STRING_AUTO_CAST_REFLECT = (f, p) -> {
        Class<?> type = f.getType();
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }

        SetterWrapper<String, Object> assinger = (v, parser) -> f.set(p, (v == null ? null : parser.convert(v)));

        if (type == String.class) {
            return (v) -> f.set(p, v);
        }
        if (type == boolean.class || type == Boolean.class) {
            return assinger.wrap(Boolean::parseBoolean);
        }
        if (type == int.class || type == Integer.class) {
            return assinger.wrap(Integer::parseInt);
        }
        if (type == byte.class || type == Byte.class) {
            return assinger.wrap(Byte::parseByte);
        }
        if (type == short.class || type == Short.class) {
            return assinger.wrap(Short::parseShort);
        }
        if (type == long.class || type == Long.class) {
            return assinger.wrap(Long::parseLong);
        }
        if (type == double.class || type == Double.class) {
            return assinger.wrap(Double::parseDouble);
        }
        if (type == float.class || type == Float.class) {
            return assinger.wrap(Float::parseFloat);
        }
        if (type == char.class || type == Character.class) {
            return assinger.wrap(v -> v.charAt(0));
        }
        throw new IllegalStateException("Type " + type.getSimpleName() + " not compatible");
    };

    /**
     * Retrieves all fields of a class, including those inherited from superclasses.
     *
     * @param type the class to inspect
     * @return a list containing all declared fields from the class and its hierarchy
     */
    @NonNull
    public static List<Field> getAllFields(@NonNull Class<?> type) {
        List<Field> fields = new ArrayList<>();
        List<List<Field>> temp = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            temp.add(Arrays.asList(c.getDeclaredFields()));
        }
        for (int i = temp.size() - 1; i >= 0; i--) {
            fields.addAll(temp.get(i));
        }
        return fields;
    }

    /**
     * Sets the value of a field, potentially bypassing access modifiers and the final keyword.
     *
     * @param clazz the class where the field is declared
     * @param target the object instance whose field should be modified (null for static fields)
     * @param fieldName the name of the field
     * @param value the new value to set
     * @param setterProvider the provider to use for field assignment (handles type conversion)
     */
    public static void setInternalState(@NonNull Class<?> clazz, @Nullable Object target, @NonNull String fieldName, @NonNull Object value,
                                        @NonNull SetterProvider setterProvider) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (Modifier.isFinal(field.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            } else {
                setterProvider.get(field, target).set(value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of a field, bypassing access modifiers.
     *
     * @param clazz the class where the field is declared
     * @param target the object instance whose field should be modified (null for static fields)
     * @param fieldName the name of the field
     * @param value the new value to set
     */
    public static void setInternalState(@NonNull Class<?> clazz, @Nullable Object target, @NonNull String fieldName, @NonNull Object value) {
        setInternalState(clazz, target, fieldName, value, NO_CAST_REFLECT);
    }

    /**
     * Retrieves the value of a field from an object instance.
     *
     * @param <T> the expected type of the field value
     * @param target the object instance
     * @param name the name of the field
     * @return the value of the field
     */
    public static <T> T getInternalState(@NonNull Object target, @NonNull String name) {
        return getInternalState(target.getClass(), target, name);
    }

    /**
     * Retrieves the value of a field, bypassing access modifiers.
     *
     * @param <T> the expected type of the field value
     * @param clazz the class where the field is declared
     * @param target the object instance (null for static fields)
     * @param name the name of the field
     * @return the value of the field
     */
    public static <T> T getInternalState(@NonNull Class<?> clazz, @Nullable Object target, @NonNull String name) {
        try {
            var declaredField = clazz.getDeclaredField(name);
            declaredField.setAccessible(true);
            return (T) declaredField.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of a field on an object instance.
     *
     * @param target the object instance
     * @param fieldName the name of the field
     * @param value the new value to set
     */
    public static void setInternalState(@NonNull Object target, @NonNull String fieldName, @NonNull Object value) {
        setInternalState(target.getClass(), target, fieldName, value);
    }

    /**
     * Functional interface for providing a {@link Setter} for a specific field.
     */
    @FunctionalInterface
    public interface SetterProvider {
        /**
         * Gets a setter for the specified field.
         *
         * @param field the field to modify
         * @param parent the target object instance
         * @return a Setter instance
         */
        Setter get(@NonNull Field field, @Nullable Object parent);
    }

    /**
     * Functional interface for wrapping a setter with custom logic.
     *
     * @param <A> the input type
     * @param <B> the converted type
     */
    @FunctionalInterface
    public interface SetterWrapper<A, B> {
        /**
         * Performs the assignment after conversion.
         *
         * @param value the input value
         * @param setter the converter to use
         * @throws Exception if an error occurs
         */
        void set(@Nullable A value, Converter<A, B> setter) throws Exception;

        /**
         * Wraps a converter into a Setter.
         *
         * @param setter the converter
         * @return a Setter instance
         */
        default Setter wrap(Converter<A, B> setter) {
            return (v) -> set((A) v, setter);
        }
    }

    /**
     * Functional interface for converting a value from one type to another.
     *
     * @param <A> the source type
     * @param <B> the target type
     */
    @FunctionalInterface
    public interface Converter<A, B> {
        /**
         * Converts the input.
         *
         * @param a the source value
         * @return the converted value
         */
        B convert(@NonNull A a);
    }

    /**
     * Functional interface for setting a value.
     */
    @FunctionalInterface
    public interface Setter {
        /**
         * Sets the value.
         *
         * @param value the value to set
         * @throws Exception if an error occurs
         */
        void set(@Nullable Object value) throws Exception;
    }

}
