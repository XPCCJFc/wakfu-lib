package wakfulib.logic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
import java.util.Stack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.internal.Internal;
import wakfulib.utils.BufferUtils;
import wakfulib.utils.StringUtils;

/**
 * Utility class to construct and encode outgoing network packets.
 * Provides a fluent API for writing various data types into a buffer.
 * Supports features like size placeholders (marks) and arch targets.
 */
@Slf4j
public class OutPacket {

    @Internal
	private final ByteBuf data;
	@Getter
    private final int packetId;
	private final Stack<Integer> marks = new Stack<>();
	private final boolean hasArchTarget;

	/**
	 * Creates an {@link OutPacket} that <b>MUST NOT</b> be sent directly to the client/server
	 * but rather be used as an intermediary buffer in the serialization process.
	 *
	 * @return A new simple buffer.
	 */
    public static OutPacket simpleBuffer() {
        return new OutPacket(ByteOrder.BIG_ENDIAN);
    }

	/**
	 * Creates an {@link OutPacket} that <b>MUST NOT</b> be sent directly to the client/server
	 * but rather be used as an intermediary buffer in the serialization process.
	 *
	 * @param byteOrder The byte order to use for the buffer.
	 * @return A new simple buffer.
	 */
	public static OutPacket simpleBuffer(ByteOrder byteOrder) {
		return new OutPacket(byteOrder);
	}

	private OutPacket(ByteOrder byteOrder) {
		data = Unpooled.buffer().order(byteOrder);
		packetId = -1;
		hasArchTarget = false;
	}

	/**
	 * Constructs a packet intended to be sent to a Wakfu Server.
	 *
	 * @param archTarget The architecture target, used for routing the packet.
	 * @param packetId The opcode of the message.
	 * @throws IllegalArgumentException if the packetId is -1.
	 */
	public OutPacket(byte archTarget, int packetId) {
		this(true, packetId);
		setArchTarget(archTarget);
	}

	/**
	 * Constructs a packet with a specified opcode.
	 *
	 * @param hasArchTarget Whether the packet should include an architecture target placeholder.
	 * @param packetId The opcode of the message.
	 * @throws IllegalArgumentException if the packetId is -1.
	 */
	public OutPacket(boolean hasArchTarget, int packetId) {
		if (packetId == -1) {
			throw new IllegalArgumentException("PacketId is -1");
		}
		this.packetId = packetId;
		data = Unpooled.buffer();
		data.writeInt(0); //Size placeholder
		this.hasArchTarget = hasArchTarget;
		if (hasArchTarget) {
			data.writeByte(0);//arch target placeholder
		}
		data.writeShort(packetId);
	}

	private void setArchTarget(byte archTarget) {
		if (hasArchTarget) {
			data.setByte(2, archTarget);
		} else {
			throw new IllegalStateException("Cannot set arch target in server to client packet");
		}
	}

	/**
	 * Completes the packet's structure by calculating and writing the total size 
	 * into the reserved size placeholder at the beginning of the buffer.
	 * This operation is mandatory before sending the packet over the network.
	 *
	 * @throws IllegalStateException if there are pending marks that haven't been closed.
	 */
	public void finish() {
		if (! marks.isEmpty()) {
			throw new IllegalStateException("Marks for outpacket is not empty ! (you must close all marks that you use before sending the packet)");
		}
		data.setInt(0, data.writerIndex());
	}

    /**
     * Gets a copy of the packet's content as a Netty {@link ByteBuf}.
     *
     * @return A copy of the internal buffer.
     * @deprecated Use {@link #getInternalBuffer()} to access the buffer directly.
     */
    @Deprecated(forRemoval = true)
	public ByteBuf getData() {
		return data.copy();
	}

    /**
     * Gets the internal Netty buffer used by this packet.
     * Use with caution as direct modifications might bypass packet structure rules.
     *
     * @return The underlying ByteBuf.
     */
    public ByteBuf getInternalBuffer() {
        return data;
    }

    /**
     * Returns the current reader index of the internal buffer.
     *
     * @return The position from which the next byte will be read.
     */
    public int readerIndex() {
        return data.readerIndex();
    }

    /**
     * Returns the current writer index of the internal buffer.
     *
     * @return The position at which the next byte will be written.
     */
     public int writerIndex() {
        return data.writerIndex();
     }

    /**
	 * Writes a single byte (8 bits) into the buffer.
	 *
	 * @param b The byte value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeByte(int b) {
		data.writeByte(b);
		return this;
	}

	/**
	 * Writes a short (16 bits) into the buffer.
	 *
	 * @param s The short value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeShort(int s) {
		data.writeShort(s);
		return this;
	}

	/**
	 * Writes an integer (32 bits) into the buffer.
	 *
	 * @param i The integer value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeInt(int i) {
		data.writeInt(i);
		return this;
	}

	/**
	 * Writes a long (64 bits) into the buffer.
	 *
	 * @param l The long value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeLong(long l) {
		data.writeLong(l);
		return this;
	}

	/**
	 * Writes an array of bytes into the buffer.
	 *
	 * @param b The byte array to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeBytes(byte[] b) {
		data.writeBytes(b);
		return this;
	}

	/**
	 * Writes a single byte into the buffer.
	 *
	 * @param b The byte value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket write(byte b) {
		data.writeByte(b);
		return this;
	}

    /**
     * Writes a boolean value as a single byte (1 for {@code true}, 0 for {@code false}).
     *
     * @param bool The boolean value to write.
     * @return This OutPacket instance for chaining.
     */
	public OutPacket writeBoolean(boolean bool) {
		data.writeByte(bool ? 1 : 0);
		return this;
	}

	/**
	 * Writes a double-precision floating point number (64 bits) into the buffer.
	 *
	 * @param d The double value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeDouble(double d) {
		data.writeDouble(d);
		return this;
	}

	/**
	 * Writes a single-precision floating point number (32 bits) into the buffer.
	 *
	 * @param f The float value to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeFloat(float f) {
		data.writeFloat(f);
		return this;
	}

	/**
	 * Writes a UTF-8 encoded string, prefixed by its length in bytes as a single byte.
	 *
	 * @param s The string to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeByteStringUTF8(String s) {
        var bytes = StringUtils.toUTF8(s);
        data.writeByte(bytes.length);
        data.writeBytes(bytes);
		return this;
	}

	/**
	 * Writes a UTF-8 encoded string, prefixed by its length in bytes as a short (2 bytes).
	 *
	 * @param s The string to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeShortStringUTF8(String s) {
        var bytes = StringUtils.toUTF8(s);
        data.writeShort(bytes.length);
        data.writeBytes(bytes);
		return this;
	}

	/**
	 * Writes a UTF-8 encoded string, prefixed by its length in bytes as an integer (4 bytes).
	 *
	 * @param s The string to write.
	 * @return This OutPacket instance for chaining.
	 */
	public OutPacket writeIntStringUTF8(String s) {
        var bytes = StringUtils.toUTF8(s);
        data.writeInt(bytes.length);
        data.writeBytes(bytes);
		return this;
	}

	/**
	 * Places a mark in the buffer to reserve a 1-byte slot for a size field.
	 * The actual size will be calculated and written when {@link #endMarkByte()} is called.
	 */
	public void markByte() {
		writeByte(0);
		marks.push(data.writerIndex());
	}

	/**
	 * Places a mark in the buffer to reserve a 2-byte slot for a size field.
	 * The actual size will be calculated and written when {@link #endMarkShort()} is called.
	 */
	public void markShort() {
		writeShort(0);
		marks.push(data.writerIndex());
	}

	/**
	 * Places a mark in the buffer to reserve a 4-byte slot for a size field.
	 * The actual size will be calculated and written when {@link #endMarkInt()} is called.
	 */
	public void markInt() {
		writeInt(0);
		marks.push(data.writerIndex());
	}

	/**
	 * Calculates the size of data written since the last 1-byte mark and fills the reserved slot.
	 */
	public void endMarkByte() {
		Integer mark = marks.pop();
		data.setByte(mark - 1, data.writerIndex() - mark);
	}

	/**
	 * Calculates the size of data written since the last 1-byte mark, 
	 * adds a constant value, and fills the reserved slot.
	 *
	 * @param add The value to add to the calculated size.
	 */
	public void endMarkByte(int add) {
		Integer mark = marks.pop();
		data.setByte(mark - 1, data.writerIndex() - mark + add);
	}

	/**
	 * Calculates the size of data written since the last 2-byte mark and fills the reserved slot.
	 */
	public void endMarkShort() {
		Integer mark = marks.pop();
		data.setShort(mark - 2, data.writerIndex() - mark);
	}

    /**
     * Fills the latest 1-byte mark with a specific value manually.
     *
     * @param value The value to write into the mark's slot.
     */
    public void setMarkedByte(int value) {
        Integer mark = marks.pop();
        data.setByte(mark - 1, value);
    }

    /**
     * Fills the latest 2-byte mark with a specific value manually.
     *
     * @param value The value to write into the mark's slot.
     */
    public void setMarkedShort(int value) {
        Integer mark = marks.pop();
        data.setShort(mark - 2, value);
    }

    /**
     * Fills the latest 4-byte mark with a specific value manually.
     *
     * @param value The value to write into the mark's slot.
     */
    public void setMarkedInt(int value) {
        Integer mark = marks.pop();
        data.setInt(mark - 4, value);
    }

	/**
	 * Calculates the size of data written since the last 2-byte mark, 
	 * adds a constant value, and fills the reserved slot.
	 *
	 * @param add The value to add to the calculated size.
	 */
	public void endMarkShort(int add) {
		Integer mark = marks.pop();
		data.setShort(mark - 2, data.writerIndex() - mark + add);
	}

	/**
	 * Calculates the size of data written since the last 4-byte mark and fills the reserved slot.
	 */
	public void endMarkInt() {
		Integer mark = marks.pop();
        var value = data.writerIndex() - mark;
        data.setInt(mark - 4, value);
	}

	/**
	 * Calculates the size of data written since the last 4-byte mark, 
	 * adds a constant value, and fills the reserved slot.
	 *
	 * @param add The value to add to the calculated size.
	 */
	public void endMarkInt(int add) {
		Integer mark = marks.pop();
		data.setInt(mark - 4, data.writerIndex() - mark + add);
	}

	/**
	 * Returns the buffer's content as a formatted string, useful for debugging.
	 *
	 * @param hex {@code true} to format as hexadecimal, {@code false} for decimal.
	 * @return A string representation of the packet's current data.
	 */
	public String getBuffer(boolean hex) {
		byte[] buf = new byte[data.writerIndex()];
		data.duplicate().getBytes(0, buf);
        return BufferUtils.toString(buf, hex);
	}

	/**
	 * Dumps the packet's content into a file for analysis or replay purposes.
	 *
	 * @param file The file path where the buffer will be written.
	 */
	public void dumpBuffer(String file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(toByteArray());
			fos.flush();
			fos.close();
		} catch (Exception e) {
            log.error("Error while dumping OutPacket to file", e);
		}
	}

	/**
	 * Returns the packet's content as a byte array.
	 *
	 * @return A new byte array containing all written data.
	 */
	public byte[] toByteArray() {
		byte[] res = new byte[data.readableBytes()];
        data.readBytes(res);
		return res;
	}
}

