package wakfu.sniff.protocol.path;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfu.sniff.beans.raw.auth.Proxy;
import wakfu.sniff.protocol.ClientProxiesResultMessage;
import wakfulib.logic.OutPacket;
import wakfulib.logic.proxy.patch.Patch;
import wakfulib.utils.ColorUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class ProxyPatch implements Patch {

    private final ClientProxiesResultMessage serializer;
    private final int newLocalPort;

    @Override
    public OutPacket patch(OutPacket outPacket, ByteBuffer data) {
        try {
            ClientProxiesResultMessage message = serializer.unserialize(data);
            List<Proxy> proxies = message.getProxies();
            if (log.isInfoEnabled()) {
                log.info(ColorUtils.ANSI_CYAN + "Proxies catched !");
                log.info("-----------------------------------");
                for (Proxy proxy : proxies) {
                    log.info(proxy.getName() + " " + proxy.getAddress() + ":" + Arrays.toString(proxy.getPorts()));
                }
            }
            for (Proxy proxy : proxies) {
                proxy.setAddress("127.0.0.1");
                proxy.setName(proxy.getName() + " Sniffed");
                proxy.setPorts(new int[]{newLocalPort});
            }

            return message.encode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
