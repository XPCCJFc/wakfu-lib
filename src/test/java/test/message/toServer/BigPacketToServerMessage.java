package test.message.toServer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Setter;
import wakfulib.internal.ArchTarget;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.internal.versionable.protocol.OpCode;
import wakfulib.internal.versionable.protocol.ToServerMessage;
import wakfulib.logic.OutPacket;

@VersionDependant @Setter
public abstract class BigPacketToServerMessage extends ToServerMessage<BigPacketToServerMessage> {
    private byte[] datas;

    @OpCode(value = 66, version = Version.TEST)
    @VersionRange(min = Version.TEST)
    @ArchTarget(0)
    public static final class BigPacketMessageV1 extends BigPacketToServerMessage {

        @Override
        public BigPacketToServerMessage unserialize(ByteBuffer buffer) {
            var bigPacketMessageV1 = new BigPacketMessageV1();
            var size = buffer.getInt();
            var bytes = new byte[size];
            buffer.get(bytes);
            bigPacketMessageV1.setDatas(bytes);
            return bigPacketMessageV1;
        }

        @Override
        public OutPacket encode() {
            var outPacket = getOutPacket();
            var rand = ThreadLocalRandom.current();
            var size = rand.nextInt(3_000, 10_000);
            outPacket.writeInt(size);
            var buff = new byte[size];
            Arrays.fill(buff, (byte) 0);
            outPacket.writeBytes(buff);
            return outPacket;
        }
    }
}
