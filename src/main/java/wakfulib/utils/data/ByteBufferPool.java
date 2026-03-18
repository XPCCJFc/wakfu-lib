package wakfulib.utils.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A thread-safe pool for reusing {@link ByteBuffer} instances.
 * Using a pool reduces the frequency of allocations and garbage collection overhead,
 * especially for fixed-size buffers used in network operations.
 */
public class ByteBufferPool {
    private static final int POOL_GROWTH_SIZE = 10;
    private final int m_byteBuffersSize;
    private final ArrayList<ByteBuffer> m_activeBuffers = new ArrayList<>();
    private final ArrayList<ByteBuffer> m_idleBuffers = new ArrayList<>();
    private final Object m_poolMutex = new Object();

    /**
     * Creates a new pool for buffers of the specified size.
     *
     * @param byteBuffersSize the size in bytes of each buffer in the pool
     */
    public ByteBufferPool(int byteBuffersSize) {
        this.m_byteBuffersSize = byteBuffersSize;
    }

    /**
     * Borrows a {@link ByteBuffer} from the pool.
     * If no idle buffers are available, the pool is expanded.
     *
     * @return an idle ByteBuffer
     */
    public ByteBuffer borrowBuffer() {
        synchronized(this.m_poolMutex) {
            if (this.m_idleBuffers.isEmpty()) {
                for(int i = 0; i < POOL_GROWTH_SIZE; ++i) {
                    this.m_idleBuffers.add(ByteBuffer.allocate(this.m_byteBuffersSize));
                }
            }

            ByteBuffer buffer = this.m_idleBuffers.remove(0);
            this.m_activeBuffers.add(buffer);
            return buffer;
        }
    }

    /**
     * Returns a {@link ByteBuffer} to the pool for later reuse.
     * The buffer is cleared before being returned to the idle list.
     *
     * @param buffer the buffer to return
     * @return {@code true} if the buffer was part of the active list and successfully returned
     * @throws IllegalArgumentException if the buffer is null
     */
    public boolean returnBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer = null");
        } else {
            synchronized(this.m_poolMutex) {
                if (!this.m_activeBuffers.contains(buffer)) {
                    return false;
                } else {
                    buffer.clear();
                    this.m_activeBuffers.remove(buffer);
                    this.m_idleBuffers.add(buffer);
                    return true;
                }
            }
        }
    }
}