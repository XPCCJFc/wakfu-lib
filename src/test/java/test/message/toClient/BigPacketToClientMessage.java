package test.message.toClient;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Setter;
import wakfulib.internal.Version;
import wakfulib.internal.VersionRange;
import wakfulib.internal.registration.VersionDependant;
import wakfulib.internal.versionable.protocol.OpCode;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.logic.OutPacket;

@VersionDependant @Setter
public abstract class BigPacketToClientMessage extends ToClientMessage<BigPacketToClientMessage> {
    private byte[] datas;
    
    @VersionRange(min = Version.TEST)
    @OpCode(value = 99, version = Version.TEST)
    public static final class BigPacketToClientMessageV1 extends BigPacketToClientMessage {

        @Override
        public BigPacketToClientMessage unserialize(ByteBuffer buffer) {
            var bigPacketMessageV1 = new BigPacketToClientMessageV1();
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
            var size = rand.nextInt(0, 10_000);
            outPacket.writeInt(size);
            var buff = new byte[size];
            rand.nextBytes(buff);
            outPacket.writeBytes(buff);
            return outPacket;
        }
    }
}
