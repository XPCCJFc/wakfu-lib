package wakfulib.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wakfulib.doc.NonNull;

/**
 * Utility class for file system and JAR resource operations.
 * Handles cross-platform path concatenation and provides methods for reading files
 * from both the local file system and embedded resources within JAR files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileHelper {

    private static final Set<JarURLConnection> URL_JAR_CONNECTIONS = new HashSet<>(1);

    /**
     * Concatenates a base file path with an appended string.
     *
     * @param base the base directory
     * @param appended the path or filename to append
     * @return the normalized concatenated path string
     */
    @NonNull
    public static String concatPath(@NonNull File base, @NonNull String appended) {
        return concatPath(base.getAbsolutePath(), appended);
    }

    /**
     * Concatenates a base path string with an appended string.
     * Ensures forward slashes are used and avoids double slashes at the junction.
     *
     * @param base the base path
     * @param appended the path or filename to append
     * @return the normalized concatenated path string
     */
    @NonNull
    public static String concatPath(@NonNull String base, @NonNull String appended) {
        if (! appended.startsWith("/")) {
            appended = '/' + appended;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return (base + appended)
            .replace('\\', '/');
    }

    /**
     * Reads the entire content of a file into a byte array.
     *
     * @param fileName the path or URL of the file to read
     * @param canBeRemovedFromCache whether to track this connection for later cache clearing
     * @return the file content as a byte array
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static byte[] readFile(@NonNull String fileName, boolean canBeRemovedFromCache) throws IOException {
        return readFullStream(openFile(fileName, canBeRemovedFromCache));
    }

    /**
     * Reads the entire content of a file into a byte array.
     *
     * @param fileName the path or URL of the file to read
     * @return the file content as a byte array
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static byte[] readFile(@NonNull String fileName) throws IOException {
        return readFullStream(openFile(fileName));
    }

    /**
     * Opens an input stream to a file.
     *
     * @param fileName the path or URL of the file to open
     * @return an open InputStream to the file
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static InputStream openFile(@NonNull String fileName) throws IOException {
        return openFile(fileName, false);
    }

    /**
     * This method opens a file from the file system or from a jar file.
     * It handles various path formats including local paths and jar:file: URLs.
     * If {@code canBeRemovedFromCache} is true, the resulting {@link JarURLConnection}
     * is tracked so it can be explicitly closed via {@link #clearJarCache()}.
     *
     * @param fileName the name or URL of the file to open
     * @param canBeRemovedFromCache whether to track the connection for manual cache clearing
     * @return an open buffered stream to the file
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static InputStream openFile(@NonNull String fileName, boolean canBeRemovedFromCache) throws IOException {
        if (! fileName.contains("file:/")) {
            fileName = "file://localhost" + (fileName.charAt(0) == '/' ? "": "/" ) + fileName;
        }
        if (fileName.contains(".jar!") && ! fileName.startsWith("jar:")) {
            fileName = "jar:" + fileName.replace("\\", "/");
        }
        InputStream stream;
        try {
            var urlConnection = new URL(fileName).openConnection();
            if (canBeRemovedFromCache && urlConnection instanceof JarURLConnection) {
                URL_JAR_CONNECTIONS.add((JarURLConnection) urlConnection);
            }
            stream = new BufferedInputStream(urlConnection.getInputStream());
        } catch (MalformedURLException var7) {
            stream = new BufferedInputStream(new FileInputStream(fileName));
        }

        return stream;
    }

    /**
     * This method reads the full content of the stream and returns it as a byte array.
     * This method closes the stream after reading even if an exception is thrown.
     * @param stream the stream to read
     * @return the full content of the stream
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    public static byte[] readFullStream(@NonNull InputStream stream) throws IOException {
        try (stream) {
            return stream.readAllBytes();
        }
    }

    /**
     * Explicitly closes all JAR file connections that were tracked during
     * calls to {@link #openFile(String, boolean)} with {@code canBeRemovedFromCache} set to true.
     * This is useful to release file locks on JAR files.
     */
    public static void clearJarCache() {
        for (JarURLConnection juc : URL_JAR_CONNECTIONS) {
            try {
                JarFile jf = juc.getJarFile();
                jf.close();
            } catch (IOException e) {
                //ignored
            }
        }
    }
}
