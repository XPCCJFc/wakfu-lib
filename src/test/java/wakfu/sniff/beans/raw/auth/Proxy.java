package wakfu.sniff.beans.raw.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import wakfu.sniff.beans.enums.Community;
import wakfulib.beans.structure.Copiable;
import wakfulib.beans.structure.Raw;
import wakfulib.exception.DeserializationException;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.logic.OutPacket;
import wakfulib.utils.StringUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@VersionDependant
public abstract class Proxy implements Raw<Proxy>, Copiable {

    private int id;
    private String name;
    private Community community;
    private String address;
    private byte order;
    private int[] ports;

    @VersionRange(min = Version.v1_91_2)
    private Integer groupId;
    @VersionRange(min = Version.v1_91_2)
    private Integer recommendedForCommunity;
    @VersionRange(min = Version.v1_91_2)
    private byte isSingleAccount;
    @VersionRange(min = Version.v1_91_2)
    private int populationLevel;
    @VersionRange(min = Version.v1_91_2)
    private byte type;
    @VersionRange(min = Version.v1_91_2)
    private int illustrationId;
    @VersionRange(min = Version.v1_91_2)
    private List<Translation> translations;

    public static record Translation(byte id, byte[] data) {

    }

    @VersionRange(min = Version.v1_91_2)
    static class ProxyV2 extends Proxy {
        @Override
        public Proxy unserialize(ByteBuffer buffer) throws DeserializationException {
            Proxy res = new ProxyV2();
            res.id = buffer.getInt();
            var hasGroup = buffer.get() == 1;
            res.groupId = hasGroup ? buffer.getInt() : null;

            int nameSize = buffer.get();
            byte[] nameSerialized = new byte[nameSize];
            buffer.get(nameSerialized);
            res.name = StringUtils.fromUTF8(nameSerialized);

            var translationSize = buffer.get();
            res.translations = new ArrayList<>(translationSize);
            for (int i = 0; i < translationSize; i++) {
                var tid = buffer.get();
                var tsize = buffer.get();
                var tdata = new byte[tsize];
                buffer.get(tdata);
                res.translations.add(new Translation(tid, tdata));
            }

            res.community = Community.getFromId(buffer.getInt());
            var isRecommanded = buffer.get() == 1;
            res.recommendedForCommunity = isRecommanded ? buffer.getInt() : null;

            byte[] utfAddress = new byte[buffer.getInt()];
            buffer.get(utfAddress);
            res.address = StringUtils.fromUTF8(utfAddress);
            res.ports = new int[buffer.getInt()];
            int i = 0;
            for (int length = res.ports.length; i < length; ++i) {
                res.ports[i] = buffer.getInt();
            }

            res.isSingleAccount = buffer.get();
            res.populationLevel = buffer.getInt();
            res.type = buffer.get();
            res.illustrationId = buffer.getInt();
            res.order = buffer.get();
            return res;
        }

        @Override
        public void serialize(OutPacket out) {
            out.writeInt(getId());
            out.writeBoolean(getGroupId() != null);
            if (getGroupId() != null) out.writeInt(getGroupId());

            out.writeByteStringUTF8(getName());

            var translations = getTranslations();
            out.write((byte) translations.size());
            for (var t : translations) {
                out.writeByte(t.id());
                out.writeByte(t.data.length);
                out.writeBytes(t.data());
            }

            out.writeInt(getCommunity().getId());
            out.writeBoolean(getRecommendedForCommunity() != null);
            if (getRecommendedForCommunity() != null) out.writeInt(getRecommendedForCommunity());

            out.writeIntStringUTF8(getAddress());
            int[] ports = getPorts();
            out.writeInt(ports.length);
            for (int port : ports) {
                out.writeInt(port);
            }

            out.writeByte(getIsSingleAccount());
            out.writeInt(getPopulationLevel());
            out.writeByte(getType());
            out.writeInt(getIllustrationId());
            out.writeByte(getOrder());
        }

        @Override
        public Object copy() {
            ProxyV2 res = new ProxyV2();
            res.setId(getId());
            res.setGroupId(getGroupId());
            res.setName(getName());
            res.setTranslations(new ArrayList<>(getTranslations()));
            res.setCommunity(getCommunity());
            res.setRecommendedForCommunity(getRecommendedForCommunity());
            int[] ports = getPorts();
            res.setPorts(Arrays.copyOf(ports, ports.length));
            res.setAddress(getAddress());
            res.setIsSingleAccount(getIsSingleAccount());
            res.setPopulationLevel(getPopulationLevel());
            res.setType(getType());
            res.setIllustrationId(getIllustrationId());
            res.setOrder(getOrder());
            return res;
        }

    }

    @VersionRange(min = Version.v1_66_1, max = Version.v1_91_2)
    static class ProxyV1 extends Proxy {

        @Override
        public Proxy unserialize(@NotNull ByteBuffer buffer) {
            Proxy res = new ProxyV1();
            res.id = buffer.getInt();
            int nameSize = buffer.getInt();
            byte[] nameSerialized = new byte[nameSize];
            buffer.get(nameSerialized);
            res.name = StringUtils.fromUTF8(nameSerialized);
            res.community = Community.getFromId(buffer.getInt());
            byte[] utfAddress = new byte[buffer.getInt()];
            buffer.get(utfAddress);
            res.address = StringUtils.fromUTF8(utfAddress);
            res.ports = new int[buffer.getInt()];
            int i = 0;
            for (int length = res.ports.length; i < length; ++i) {
                res.ports[i] = buffer.getInt();
            }
            res.order = buffer.get();
            return res;
        }

        @Override
        public void serialize(OutPacket out) {
            out.writeInt(getId());
            out.writeIntStringUTF8(getName());
            out.writeInt(getCommunity().getId());
            out.writeIntStringUTF8(getAddress());
            int[] ports = getPorts();
            out.writeInt(ports.length);
            for (int port : ports) {
                out.writeInt(port);
            }
            out.write(getOrder());
        }

        @Override
        public Object copy() {
            ProxyV1 res = new ProxyV1();
            res.setId(getId());
            int[] ports = getPorts();
            res.setPorts(Arrays.copyOf(ports, ports.length));
            res.setAddress(getAddress());
            res.setCommunity(getCommunity());
            res.setOrder(getOrder());
            return res;
        }
    }
}
