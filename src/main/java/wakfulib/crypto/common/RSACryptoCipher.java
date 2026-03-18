package wakfulib.crypto.common;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.Cipher;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.exception.CryptoException;

@Slf4j
public class RSACryptoCipher extends CryptoCipher {

    public static final AlgorithmParameterSpec parameterSpec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
    private KeyFactory factory;

    public RSACryptoCipher(String algoName, AlgorithmParameterSpec spec) {
        super(algoName, spec);

        try {
            factory = KeyFactory.getInstance(algorithmName);
        } catch (Exception e) {
            log.error("Error while getting a keyfactory for algo " + this.algorithmName, e);
        }
    }

    public void init(byte[] keyData) {
        try {
            EncodedKeySpec keySpec = createKeySpec(keyData);
            PublicKey key = factory.generatePublic(keySpec);
            cipher.init(1, key);
        } catch (Exception e) {
            log.error("Error while init RSACryptoCipher", e);
        }
    }

    public void initRSA(BigInteger modulus, BigInteger privateKey) {
        try {
            RSAPrivateKeySpec rsaspec = new RSAPrivateKeySpec(modulus, privateKey);
            PrivateKey pub = factory.generatePrivate(rsaspec);
            cipher.init(Cipher.DECRYPT_MODE, pub);
        } catch (Exception e) {
            log.error("Error while init RSACryptoCipher with PK and %", e);
        }
    }
    
    @Override
    @NonNull
    public byte[] encode(@NonNull byte[] data) throws CryptoException {
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    @Override
    @NonNull
    public byte[] decode(@NonNull byte[] data) throws CryptoException {
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

}
