package wakfulib.ui.proxy.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SSLPacketHS implements Packet {
    boolean fromServer;

    @Override
    public String toString() {
        return (fromServer ? "< " : "> ") + "SSL";
    }
}
