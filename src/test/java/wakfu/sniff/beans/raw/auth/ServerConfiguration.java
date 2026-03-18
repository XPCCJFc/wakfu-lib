package wakfu.sniff.beans.raw.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import wakfulib.beans.structure.Raw;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.logic.OutPacket;
import wakfulib.utils.StringUtils;

import java.nio.ByteBuffer;

@Getter
@Setter
@ToString
@VersionDependant
public abstract class ServerConfiguration implements Raw<ServerConfiguration> {

    private String value;
    private short type;

    @VersionRange(min = Version.v1_66_1)
    static class ServerConfigurationV1 extends ServerConfiguration {

        @Override
        public ServerConfiguration unserialize(@NotNull ByteBuffer buffer) {
            ServerConfiguration res = new ServerConfigurationV1();
            res.type = buffer.getShort();
            byte[] bytes = new byte[buffer.getInt()];
            buffer.get(bytes);
            res.value = StringUtils.fromUTF8(bytes);
            return res;
        }

        @Override
        public void serialize(OutPacket out) {
            out.writeShort(getType());
            out.writeIntStringUTF8(getValue());
        }
    }
}

