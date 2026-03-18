package wakfulib.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for byte array compression using the Zlib algorithm.
 * Uses standard Java {@link DeflaterOutputStream} and {@link InflaterOutputStream}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompressionUtils {

    /**
     * Compresses the provided byte array using the Deflate algorithm.
     *
     * @param t the raw byte array to compress
     * @return the compressed byte array
     * @throws IOException if a compression error occurs
     */
    public static byte[] compress(byte[] t) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Throwable var2 = null;

        byte[] var5;
        try {
            DeflaterOutputStream dos = new DeflaterOutputStream(bos);
            Throwable var4 = null;

            try {
                dos.write(t);
                dos.finish();
                dos.flush();
                var5 = bos.toByteArray();
            } catch (Throwable var28) {
                var4 = var28;
                throw var28;
            } finally {
                if (var4 != null) {
                    try {
                        dos.close();
                    } catch (Throwable var27) {
                        var4.addSuppressed(var27);
                    }
                } else {
                    dos.close();
                }

            }
        } catch (Throwable var30) {
            var2 = var30;
            throw var30;
        } finally {
            if (var2 != null) {
                try {
                    bos.close();
                } catch (Throwable var26) {
                    var2.addSuppressed(var26);
                }
            } else {
                bos.close();
            }
        }

        return var5;
    }

    /**
     * Decompresses the provided byte array using the Inflate algorithm.
     *
     * @param t the compressed byte array
     * @return the decompressed (raw) byte array
     * @throws IOException if a decompression error occurs
     */
    public static byte[] unCompress(byte[] t) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Throwable var2 = null;

        byte[] var5;
        try {
            InflaterOutputStream dos = new InflaterOutputStream(bos);
            Throwable var4 = null;

            try {
                dos.write(t);
                dos.flush();
                var5 = bos.toByteArray();
            } catch (Throwable var28) {
                var4 = var28;
                throw var28;
            } finally {
                if (var4 != null) {
                    try {
                        dos.close();
                    } catch (Throwable var27) {
                        var4.addSuppressed(var27);
                    }
                } else {
                    dos.close();
                }
            }
        } catch (Throwable var30) {
            var2 = var30;
            throw var30;
        } finally {
            if (var2 != null) {
                try {
                    bos.close();
                } catch (Throwable var26) {
                    var2.addSuppressed(var26);
                }
            } else {
                bos.close();
            }

        }

        return var5;
    }
}
