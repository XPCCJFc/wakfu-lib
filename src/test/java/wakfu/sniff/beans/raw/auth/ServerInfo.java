package wakfu.sniff.beans.raw.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import wakfulib.beans.structure.Raw;
import wakfulib.internal.Inject;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.logic.OutPacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@VersionDependant
public abstract class ServerInfo implements Raw<ServerInfo> {

    private int id;
    private byte[] version;
    private List<ServerConfiguration> serverConfigurations = new ArrayList<>();
    private boolean locked;

    @VersionRange(min = Version.v1_66_1)
    static class ServerInfoV1 extends ServerInfo {

        @Inject ServerConfiguration serverConfigurationSerializer;

        @Override
        public ServerInfo unserialize(@NotNull ByteBuffer buffer) {
            ServerInfo res = new ServerInfoV1();
            res.id = buffer.getInt();
            res.version = new byte[buffer.getInt()];
            buffer.get(res.version);
            int s = buffer.getInt(); //size unused
//            res.bytes = new byte[s];
//            buffer.get(res.bytes);
            int numProperties = buffer.getInt();
            for (int i = 0; i < numProperties; i++) {
                res.serverConfigurations.add(serverConfigurationSerializer.unserialize(buffer));
            }
            res.locked = buffer.get() != 0;
            return res;
        }

        @Override
        public void serialize(OutPacket out) {
            out.writeInt(getId());
            byte[] version = getVersion();
            if (version == null) {
                version = Version.getCurrent().getAsByte();
            }
            out.writeInt(version.length);
            out.writeBytes(version);
            out.markInt();
            List<ServerConfiguration> serverConfigurations = getServerConfigurations();
            out.writeInt(serverConfigurations.size());
            for (ServerConfiguration serverConfiguration : serverConfigurations) {
                serverConfiguration.serialize(out);
            }
            out.endMarkInt();
            out.writeBoolean(isLocked());
        }
    }
}

