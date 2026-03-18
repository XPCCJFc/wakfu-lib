package wakfulib.crypto.common;

import wakfulib.doc.NonNull;
import wakfulib.exception.CryptoException;

/**
 * Interface for providing cryptographic services such as encoding and decoding.
 */
public interface CryptoService {

    /**
     * Encodes the provided data.
     *
     * @param data The data to encode.
     * @return The encoded byte array.
     * @throws CryptoException if an error occurs during encoding.
     */
    @NonNull 
    byte[] encode(@NonNull byte[] data) throws CryptoException;

    /**
     * Decodes the provided data.
     *
     * @param data The data to decode.
     * @return The decoded byte array.
     * @throws CryptoException if an error occurs during decoding.
     */
    @NonNull 
    byte[] decode(@NonNull byte[] data) throws CryptoException;

}
