package wakfulib.logic.pipeline;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.nio.ByteBuffer;

public class DataNettyBuffer {
    @Getter
    private final ByteBuffer mBuffer;
    private final ByteBuf mNettyBuf;

    public DataNettyBuffer(ByteBuf bytebuf) {
        // throws exception if there are multiple nioBuffers, or reference count is not 1
        assert bytebuf.nioBufferCount() == 1: "Number of nioBuffers of this bytebuf is " + bytebuf.nioBufferCount() + " (1 expected).";
        assert bytebuf.refCnt() == 1: "Reference count of this bytebuf is " + bytebuf.refCnt() + " (1 expected).";

        // increase the bytebuf reference count so it would not be recycled by Netty
        bytebuf.retain();
        mNettyBuf = bytebuf;
        mBuffer = bytebuf.nioBuffer();
    }

    public void release() {
        mNettyBuf.release(2);
    }
}
