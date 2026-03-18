package wakfulib.crypto.common;


import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CryptoCipher implements CryptoService {

    @Getter
    protected final String algorithmName;
    protected final AlgorithmParameterSpec parameterSpecs;
    protected Cipher cipher;
    
    protected CryptoCipher(String algoName, AlgorithmParameterSpec parameterSpec) {
        algorithmName = algoName;
        parameterSpecs = parameterSpec;

        try {
            cipher = Cipher.getInstance(this.algorithmName);
        } catch (Exception e) {
            log.error("Error while getting cipher instance for algo " + this.algorithmName, e);
        }
    }

    protected EncodedKeySpec createKeySpec(byte[] key) {
        return new X509EncodedKeySpec(key);
    }

}
