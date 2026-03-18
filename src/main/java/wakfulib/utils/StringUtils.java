package wakfulib.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for string manipulation and conversions.
 * Provides support for capitalization, UTF-8 encoding/decoding, 
 * and conversions between byte arrays and formatted hex strings.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    /**
     * Capitalizes the first letter of the given string.
     *
     * @param toCapitalize the string to capitalize
     * @return the capitalized string, or the original if empty
     */
    public static String capitalize(@NonNull String toCapitalize) {
      if (toCapitalize.length() == 0) return toCapitalize;
      return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1);
    }

    /**
     * Converts a string to a UTF-8 encoded byte array.
     *
     * @param s the string to encode
     * @return the UTF-8 byte array, or an empty array if the string is null
     */
    public static byte[] toUTF8(String s) {
        if (s == null) {
            return Empty.EMPTY_BYTE_ARRAY;
        } else {
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Decodes a UTF-8 encoded byte array into a string.
     *
     * @param b the byte array to decode
     * @return the decoded string
     */
    public static String fromUTF8(byte[] b) {
        return fromUTF8(b, 0, b.length);
    }

    /**
     * Decodes a portion of a UTF-8 encoded byte array into a string.
     *
     * @param b the byte array to decode
     * @param offset the starting position in the array
     * @param length the number of bytes to decode
     * @return the decoded string, or {@code null} if the array is null
     */
    public static String fromUTF8(byte[] b, int offset, int length) {
        if (b == null) {
            return null;
        } else if (length == 0) {
            return "";
        } else {
            return new String(b, offset, length, StandardCharsets.UTF_8);
        }
    }

    /**
     * Converts a byte array into a human-readable hex string.
     * Bytes are formatted as two-digit hexadecimal values separated by spaces.
     *
     * @param data the byte array to format
     * @return a StringBuilder containing the formatted hex string
     */
    public static StringBuilder byteArrayToFormattedString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (byte b : data) {
            sb.append(String.format("%02x", b));
            sb.append(" ");
            if (i % 16 == 0) {
                sb.append(System.lineSeparator());
            }
            i++;
        }
        return sb;
    }

    /**
     * Converts a formatted hex string back into a byte array.
     * Whitespace and newlines are ignored.
     *
     * @param str the hex string to parse
     * @return the resulting byte array
     */
    public static byte[] formattedStringToByteArray(String str) {
        String s = str.replaceAll("\n", "").replaceAll(" ", "");
        char[] chars = s.toCharArray();
        ByteBuf buffer = Unpooled.buffer();
        for (int i = 0; i < chars.length; i = i + 2) {
            buffer.writeByte((byte) ((Character.digit(chars[i], 16) << 4) + Character.digit(chars[i + 1], 16)));
        }
        byte[] res = new byte[buffer.writerIndex()];
        buffer.readBytes(res);
        return res;
    }
    
    /**
     * Returns the hexadecimal representation of the object's identity hash code.
     *
     * @param o the object
     * @return the identity hash code as a hex string
     */
    public static String getObjectIdentity(@NonNull Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }


    /**
     * Checks if a string is not null and contains non-whitespace characters.
     *
     * @param str the string to check
     * @return {@code true} if the string is not empty after trimming, {@code false} otherwise
     */
    public static boolean isTrimmedNotEmpty(@Nullable String str) {
        return str != null && !str.isEmpty() && !str.trim().isEmpty();
    }

    /**
     * Sanitizes a string to be used as a valid identifier by replacing non-Latin characters with underscores.
     *
     * @param str the string to sanitize
     * @return the sanitized string
     */
    public static String getIdentifier(String str) {
        return str.replaceAll("[^\\p{InBasic_Latin}\\p{InLatin-1Supplement}]", "_");
    }
}
