package wakfulib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for advanced string formatting with support for conditionals and plurals.
 * <p>
 * Supported syntax:
 * <ul>
 *   <li><b>Conditional:</b> {@code {[index?valueIfTrue:valueIfFalse]}}</li>
 *   <li><b>Plural:</b> {@code {[index>1?plural:singular]}}</li>
 *   <li><b>Argument Injection:</b> {@code [#index]} (1-based index)</li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringFormatter {

    private static final Pattern CONDITION_GLOBAL_PATTERN = Pattern.compile("\\{((\\[[^\\[\\]{}?:]*\\])+)\\?([^\\}]*):([^\\}]*)\\}");
    private static final Pattern CONDITION_LOCAL_PATTERN = Pattern.compile("\\[([\\~\\*\\>])([^\\[\\]]+)\\]");
    private static final Pattern REPLACE_BY_ARGS_PATTERN = Pattern.compile("\\[\\#([0-9]+)\\]");

    /**
     * Formats a string by resolving conditional blocks and injecting arguments.
     *
     * @param string the template string
     * @param args the arguments to use for formatting
     * @return the formatted string
     */
    public static String format(String string, Object... args) {
        StringBuilder formattedString = new StringBuilder();
        Matcher matcher = CONDITION_GLOBAL_PATTERN.matcher(string);

        while(matcher.find()) {
            boolean conditionResult = false;
            String conditionGroup = matcher.group(1);
            Matcher localMatcher = CONDITION_LOCAL_PATTERN.matcher(conditionGroup);

            while(localMatcher.find()) {
                char conditionChar = localMatcher.group(1).charAt(0);
                int conditionArg = Integer.parseInt(localMatcher.group(2));
                switch (conditionChar) {
                    case '*':
                        break;
                    case '>':
                        if (args.length >= conditionArg) {
                            conditionResult = isPlural(args[conditionArg - 1]);
                        }
                        break;
                    case '~':
                        conditionResult = args.length >= conditionArg && args[conditionArg - 1] != null && isSuperiorAt(args[conditionArg - 1], 0);
                        break;
                    default:
                        log.error("- format(): Unable to format expression '{}'", string);
                }
            }

            if (conditionResult) {
                matcher.appendReplacement(formattedString, matcher.group(3));
            } else {
                matcher.appendReplacement(formattedString, matcher.group(4));
            }
        }

        matcher.appendTail(formattedString);
        matcher = REPLACE_BY_ARGS_PATTERN.matcher(formattedString.toString());
        formattedString = new StringBuilder();

        while(matcher.find()) {
            int argIndex = Integer.parseInt(matcher.group(1)) - 1;
            if (args.length > argIndex) {
                matcher.appendReplacement(formattedString, args[argIndex].toString());
            } else {
                matcher.appendReplacement(formattedString, "");
            }
        }

        matcher.appendTail(formattedString);
        return formattedString.toString();
    }

    private static boolean isPlural(Object object) {
        return isSuperiorAt(object, 1);
    }

    private static boolean isSuperiorAt(Object object, int value) {
        if (object instanceof Integer) {
            return (Integer)object > value;
        } else if (object instanceof Float) {
            return (Float)object > (float)value;
        } else if (object instanceof Double) {
            return (Double)object > (double)value;
        } else if (object instanceof Short) {
            return (Short)object > value;
        } else if (object instanceof Byte) {
            return (Byte)object > value;
        } else if (object instanceof String) {
            return Double.parseDouble((String)object) > (double)value;
        } else {
            return false;
        }
    }
}
