package wakfulib.logic.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class SSLContextHolder {
    public static SslContext forServer() throws Exception {
        SelfSignedCertificate certificate = new SelfSignedCertificate();
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(certificate.certificate(), certificate.privateKey());
        return sslContextBuilder.build();
    }

    public static SslContext forClient() throws Exception {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        return sslContextBuilder.build();
    }
}
