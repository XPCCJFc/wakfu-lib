package wakfulib.crypto.client;

import static wakfulib.crypto.common.RSACryptoCipher.parameterSpec;

import java.nio.ByteBuffer;
import wakfulib.crypto.common.RSACryptoCipher;
import wakfulib.doc.NonNull;
import wakfulib.exception.CryptoException;
import wakfulib.utils.StringUtils;

public class ConnectionEncryptionManager {
    private static ConnectionEncryptionManager INSTANCE;
    private final RSACryptoCipher rsa;

    public static ConnectionEncryptionManager getInstance() {
        return INSTANCE == null ? INSTANCE = new ConnectionEncryptionManager() : INSTANCE;
    }

    public ConnectionEncryptionManager() {
        rsa = new RSACryptoCipher("RSA", parameterSpec);
    }

    public void init(byte[] keyData) {
        rsa.init(keyData);
    }

    @NonNull
    public byte[] crypt(@NonNull byte[] rawData) throws CryptoException {
        return rsa.encode(rawData);
    }

    public static byte[] getEncryptedLoginAndPassword(long salt, String loginS, String mdpS) throws CryptoException {
        byte[] login = StringUtils.toUTF8(loginS);
        byte loginLength = (byte)login.length;
        byte[] password = StringUtils.toUTF8(mdpS);
        byte passwordLength = (byte)password.length;
        ByteBuffer bb = ByteBuffer.allocate(9 + loginLength + 1 + passwordLength);
        bb.putLong(salt);
        bb.put(loginLength);
        bb.put(login);
        bb.put(passwordLength);
        bb.put(password);
        byte[] rawData = bb.array();
        return ConnectionEncryptionManager.INSTANCE.crypt(rawData);
    }

    public static byte[] getEncryptedToken(long salt, String token) throws CryptoException {
        byte[] tokenB = StringUtils.toUTF8(token);
        byte tokenLength = (byte)tokenB.length;
        ByteBuffer bb = ByteBuffer.allocate(9 + tokenLength);
        bb.putLong(salt);
        bb.put(tokenLength);
        bb.put(tokenB);
        byte[] rawData = bb.array();
        return ConnectionEncryptionManager.INSTANCE.crypt(rawData);
    }
}
