package wakfulib.crypto.server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import wakfulib.crypto.common.CryptoService;
import wakfulib.crypto.common.RSACryptoCipher;
import wakfulib.doc.NonNull;
import wakfulib.exception.CryptoException;

public class RSACertificateManager implements CryptoService {

    public static final RSACertificateManager INSTANCE = new RSACertificateManager();

    private final PublicKey publicKey;
    private final RSACryptoCipher encrypt;
    private final RSACryptoCipher decrypt;

    public RSACertificateManager() {
        SecureRandom random = new SecureRandom();
        KeyPairGenerator kpGen;
        try {
            kpGen = KeyPairGenerator.getInstance("RSA");
            kpGen.initialize(1024, random);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        KeyPair pair = kpGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        publicKey = pair.getPublic();

        encrypt = new RSACryptoCipher("RSA", RSACryptoCipher.parameterSpec);
        decrypt = new RSACryptoCipher("RSA", RSACryptoCipher.parameterSpec);

        encrypt.init(publicKey.getEncoded());
        decrypt.initRSA(((RSAPrivateKey) privateKey).getModulus(), ((RSAPrivateKey) privateKey).getPrivateExponent());
    }

    @NonNull
    @Override
    public byte[] encode(@NonNull byte[] data) throws CryptoException {
        return encrypt.encode(data);
    }
    @NonNull
    @Override
    public byte[] decode(@NonNull byte[] data) throws CryptoException {
        return decrypt.encode(data);
    }

    public byte[] getPublicKey() {
        return publicKey.getEncoded();
    }

}
