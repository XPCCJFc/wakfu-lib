package wakfulib.logic.basic.client.pipeline;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import wakfulib.doc.NonNull;
import wakfulib.exception.NotImplementedException;

public enum ProtocolAdapter {
    CLIENT_SERVER {
        @Override
        public ByteBuffer adapt(@NonNull ByteBuf buf) {
            int msgSize = buf.readShort() & '\uffff';
            byte[] rawData = new byte[msgSize - 2];
            buf.readBytes(rawData);
            ByteBuffer bb = ByteBuffer.allocate(msgSize);
            bb.putShort((short) msgSize);
            bb.put(rawData);
            bb.rewind();
            return bb;
        }
    },
    SERVER_PROXY {
        @Override
        public ByteBuffer adapt(@NonNull ByteBuf buf) {
            int msgSize = buf.readShort() & '\uffff';
            short msgType = buf.readShort();
            byte[] rawData = new byte[msgSize - 4];
            buf.readBytes(rawData);
            ByteBuffer bb = ByteBuffer.allocate(msgSize);
            bb.putShort((short) msgSize);
            bb.putShort(msgType);
            bb.put(rawData);
            bb.rewind();
            return bb;
        }
    },
    INTER_SERVER {
        @Override
        public ByteBuffer adapt(@NonNull ByteBuf buf) {
            int msgSize = buf.readInt() & Integer.MAX_VALUE;
            short msgType = buf.readShort();
            byte[] rawData = new byte[msgSize - 6];
            buf.readBytes(rawData);
            ByteBuffer bb = ByteBuffer.allocate(msgSize);
            bb.putInt(msgSize);
            bb.putShort(msgType);
            bb.put(rawData);
            bb.rewind();
            return bb;
        }
    };

    @NonNull
    public ByteBuffer adapt(@NonNull ByteBuf buf) {
        throw new NotImplementedException();
    }
}
