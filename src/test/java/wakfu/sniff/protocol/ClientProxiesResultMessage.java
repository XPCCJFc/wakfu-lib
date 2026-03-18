package wakfu.sniff.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import wakfu.sniff.beans.raw.auth.Proxy;
import wakfu.sniff.beans.raw.auth.ServerInfo;
import wakfulib.doc.NonNull;
import wakfulib.internal.Inject;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.internal.versionable.protocol.OpCode;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.logic.OutPacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Research :
 *  • 2 Fields of type : TIntObjectHashMap
 */
@ToString
@Getter
@Setter
@VersionDependant
public abstract class ClientProxiesResultMessage extends ToClientMessage<ClientProxiesResultMessage> {
    @VersionRange(min = Version.v1_66_1)
    private List<Proxy> proxies = new ArrayList<>();
    @VersionRange(min = Version.v1_66_1)
    private List<ServerInfo> serverInfos = new ArrayList<>();

    @VersionRange(min = Version.v1_63_0)
    @OpCode(value = 1036, version = Version.v1_63_0)
    @OpCode(value = 573, version = Version.v1_66_1)
    @OpCode(value = 564, version = Version.v1_67_2)
    @OpCode(value = 493, version = Version.v1_68_0)
    @OpCode(value = 584, version = Version.v1_69_0)
    @OpCode(value = 552, version = Version.v1_70_4)
    @OpCode(value = 424, version = Version.v1_72_1)
    @OpCode(value = 425, version = Version.v1_74_1)
    @OpCode(value = 425, version = Version.v1_74_4)
    @OpCode(value = 441, version = Version.v1_75_4)
    @OpCode(value = 427, version = Version.v1_91_2)
    static class ClientProxiesResultMessageV1 extends ClientProxiesResultMessage {

        @Inject Proxy proxySerializer;
        @Inject ServerInfo serverInfoSerializer;

        @Override
        public ClientProxiesResultMessage unserialize(@NonNull ByteBuffer buffer) {
            ClientProxiesResultMessage res = VersionRegistry.messageInstance(ClientProxiesResultMessage.class);
            int proxySize = buffer.getInt();
            for (int i = 0; i < proxySize; i++) {
                res.proxies.add(proxySerializer.unserialize(buffer));
            }
            int infoSize = buffer.getInt();
            for (int i = 0; i < infoSize; i++) {
                res.serverInfos.add(serverInfoSerializer.unserialize(buffer));
            }
            return res;
        }

        @Override
        public @NotNull OutPacket encode() {
            OutPacket outPacket = new OutPacket(false, getOpCode());
            List<Proxy> proxies = getProxies();
            outPacket.writeInt(proxies.size());
            for (Proxy proxy : proxies) {
                proxy.serialize(outPacket);
            }
            List<ServerInfo> serverInfos = getServerInfos();
            outPacket.writeInt(serverInfos.size());
            for (ServerInfo serverInfo : serverInfos) {
                serverInfo.serialize(outPacket);
            }
            return outPacket;
        }
    }
}
