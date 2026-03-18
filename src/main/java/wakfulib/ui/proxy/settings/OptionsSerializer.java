package wakfulib.ui.proxy.settings;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;

/**
 * Gère les sauvegardes et chargements des différentes préférences de l'application vers et depuis un fichier.
 */
@Slf4j
public class OptionsSerializer {

    /**
     * Load the options from the properties file
     * @param propertiesFile
     * @param optionsContainer a class containing options (non final fields)
     * @return true if the option container has new options that were not in the properties file
     */
    public static boolean loadFromProperties(Properties propertiesFile, Object optionsContainer) {
        boolean hasNewOptions = false;
        try {
            for (Field optionsField : optionsContainer.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(optionsField.getModifiers())) continue;
                optionsField.setAccessible(true);
                final String property = propertiesFile.getProperty(optionsField.getName());
                if (property == null) {
                    hasNewOptions = true;
                    OptionsSerialisationStrategyImpl.getStrategy(optionsField.getType()).process(optionsField, propertiesFile, optionsContainer);
                    continue;
                }
                Object propValue = OptionsUnserialisationStrategyImpl.getStrategy(optionsField.getType()).process(optionsField, propertiesFile);
                if (propValue == null) {
                    log.warn("Unable to find option '{}' in clazz '{}'", optionsField.getName(), optionsContainer.getClass().getName());
                } else {
                    optionsField.set(optionsContainer, propValue);
                }
            }
        } catch (IllegalAccessException e) {
            //cannot happen
        }
        return hasNewOptions;
    }

    public static void saveToProperties(Properties propertiesFile, Object optionsContainer) {
        try {
            for (Field optionsField : optionsContainer.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(optionsField.getModifiers())) continue;
                optionsField.setAccessible(true);
                OptionsSerialisationStrategyImpl.getStrategy(optionsField.getType()).process(optionsField, propertiesFile, optionsContainer);
            }
        } catch (IllegalAccessException e) {
            //cannot happen
        }
    }

    @FunctionalInterface
    private interface OptionsUnserialisationStrategy {
        @Nullable
        default Object process(@NonNull Field properyField, @NonNull Properties propertiesFile) {
            var property = propertiesFile.getProperty(properyField.getName());
            if (property == null) return null;
            return unserialize(properyField.getType(),property);
        }

        @Nullable
        Object unserialize(@NonNull Class<?> propertyType, @NonNull String value);
    }

    @AllArgsConstructor
    private static class ArrayOptionUnserialisationStrategy implements OptionsUnserialisationStrategy {

        private final OptionsUnserialisationStrategy elementStrategy;

        @Override
        public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
            Class<?> componentType = propertyType.getComponentType();
            if ("[]".equals(value)) {
                return Array.newInstance(componentType, 0);
            }
            var splitted = value.substring(1, value.length() - 1).split("(?<!\\\\)" + Pattern.quote(","));
            var res = Array.newInstance(componentType, splitted.length);
            for (int i = 0; i < splitted.length; i++) {
                String s = splitted[i];
                if (s.isEmpty()) {
                    continue;
                }
                if ("null".equals(s)) {
                    Array.set(res, i, null);
                } else {
                    Array.set(res, i, elementStrategy.unserialize(componentType,
                        s.substring(1, s.length() - 1) //remove quotes
                            .replace("\\,", ",") //unescape commas
                    ));
                }
            }
            return res;
        }
    }

    private enum OptionsUnserialisationStrategyImpl implements OptionsUnserialisationStrategy {
        STRING {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return value;
            }
        },
        SHORT {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return Short.parseShort(value);
            }
        },
        INTEGER {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return Integer.parseInt(value);
            }
        },
        BOOLEAN {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return Boolean.parseBoolean(value);
            }
        },
        COLOR {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return new Color(Integer.parseInt(value), true);
            }
        },
        FILE {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return new File(value);
            }
        },
        ENUM {
            @Override
            public Object unserialize(@NonNull Class<?> propertyType, @NonNull String value) {
                return Enum.valueOf((Class<Enum>) propertyType, value);
            }
        };

        private static OptionsUnserialisationStrategyImpl getStrategyImpl(Class<?> toEvaluate) {
            if (toEvaluate == Integer.class || toEvaluate == int.class) {
                return OptionsUnserialisationStrategyImpl.INTEGER;
            }
            if (toEvaluate == Short.class || toEvaluate == short.class) {
                return OptionsUnserialisationStrategyImpl.SHORT;
            }
            if (toEvaluate == boolean.class || toEvaluate == Boolean.class) {
                return OptionsUnserialisationStrategyImpl.BOOLEAN;
            }
            if (toEvaluate == Color.class) {
                return OptionsUnserialisationStrategyImpl.COLOR;
            }
            if (toEvaluate == File.class) {
                return OptionsUnserialisationStrategyImpl.FILE;
            }
            if (toEvaluate.isEnum()) {
                return OptionsUnserialisationStrategyImpl.ENUM;
            }
            return OptionsUnserialisationStrategyImpl.STRING;
        }

        private static OptionsUnserialisationStrategy getStrategy(Class<?> toEvaluate) {
            if (toEvaluate.isArray()) {
                //we don't support nested arrays
                return new ArrayOptionUnserialisationStrategy(getStrategyImpl(toEvaluate.getComponentType()));
            } else {
                return getStrategyImpl(toEvaluate);
            }
        }
    }

    @FunctionalInterface
    private interface OptionsSerialisationStrategy {
        default void process(Field properyField, Properties propertiesFile, Object optionsContainer) throws IllegalAccessException {
            var value = properyField.get(optionsContainer);
            if (value == null) return;
            propertiesFile.setProperty(properyField.getName(), serializeValue(properyField, value));
        }
        @Nullable
        String serializeValue(@NonNull Field properyField, @NonNull Object value) throws IllegalAccessException;
    }

    @AllArgsConstructor
    private static class ArrayOptionSerialisationStrategy implements OptionsSerialisationStrategy {
        private final OptionsSerialisationStrategy elementStrategy;

        @Override
        public String serializeValue(Field properyField, Object array) throws IllegalAccessException {
            var stringBuilder = new StringBuilder();
            stringBuilder.append('[');
            var length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                var str = elementStrategy.serializeValue(properyField, Array.get(array, i));
                if (str == null) {
                    stringBuilder.append("null");
                } else {
                    stringBuilder.append('"').append(str.replace(",", "\\,")).append('"');
                }
            }
            return stringBuilder.append(']').toString();
        }
    }

    /**
     * Stratégie de serialisation des différents types d'attributs des options
     */
    private enum OptionsSerialisationStrategyImpl implements OptionsSerialisationStrategy {
        COLOR {
            @Override
            public String serializeValue(Field properyField, Object value) throws IllegalAccessException {
                var color = (Color) value;
                if (color == null) return null;
                return Integer.toString(color.getRGB());
            }
        },
        FILE {
            @Override
            public String serializeValue(Field properyField, Object value) throws IllegalAccessException {
                File file = (File) value;
                if (file == null) return null;
                return file.getAbsolutePath();
            }
        },
        ENUM {
            @Override
            public String serializeValue(Field properyField, Object value) throws IllegalAccessException {
                var anEnum = (Enum) value;
                if (anEnum == null) return null;
                return anEnum.name();
            }
        },
        STRING {
            @Override
            public String serializeValue(Field properyField, Object value) throws IllegalAccessException {
                if (value == null) return null;
                return Objects.toString(value);
            }
        };

        @Override
        public String serializeValue(Field properyField, Object optionsContainer) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        private static OptionsSerialisationStrategyImpl getStrategyImpl(Class<?> toEvaluate) {
            if (toEvaluate == Color.class) {
                return OptionsSerialisationStrategyImpl.COLOR;
            }
            if (toEvaluate == File.class) {
                return OptionsSerialisationStrategyImpl.FILE;
            }
            if (toEvaluate.isEnum()) {
                return OptionsSerialisationStrategyImpl.ENUM;
            }
            return OptionsSerialisationStrategyImpl.STRING;
        }

        private static OptionsSerialisationStrategy getStrategy(Class<?> toEvaluate) {
            if (toEvaluate.isArray()) {
                //we don't support nested arrays
                return new ArrayOptionSerialisationStrategy(getStrategyImpl(toEvaluate.getComponentType()));
            } else {
                return getStrategyImpl(toEvaluate);
            }
        }
    }
}
