package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import wakfulib.logic.OutPacket;
import wakfulib.utils.BufferUtils;
import wakfulib.utils.data.BinarSerialWrapper;

public class BinarWrapperTest {

    @Test
    public void testIndexes() {
        var outPacket = OutPacket.simpleBuffer();

        var binarSerialWrapper = new BinarSerialWrapper(outPacket, 1);
        outPacket.writeShortStringUTF8("test");
        binarSerialWrapper.endPart(9);


        System.out.println(outPacket.getBuffer(true));

        var resp = readBinarLikeTheGame(outPacket.toByteArray());
        assertEquals(1, resp.size());
        assertTrue(resp.containsKey(9));
        var data = resp.get(9);
        assertEquals("test", BufferUtils.getShortStringUTF8(data));
    }

    @Test
    public void testIndexes_2Parts() {
        var outPacket = OutPacket.simpleBuffer();

        var binarSerialWrapper = new BinarSerialWrapper(outPacket, 2);
        outPacket.writeShortStringUTF8("abcdefghi");
        binarSerialWrapper.endPart(9);

        outPacket.writeShortStringUTF8("ABCDE");
        binarSerialWrapper.endPart(5);

        System.out.println(outPacket.getBuffer(true));

        var resp = readBinarLikeTheGame(outPacket.toByteArray());
        assertEquals(2, resp.size());
        assertTrue(resp.containsKey(9));
        var data = resp.get(9);
        assertEquals("abcdefghi", BufferUtils.getShortStringUTF8(data));
        assertTrue(resp.containsKey(5));
        data = resp.get(5);
        assertEquals("ABCDE", BufferUtils.getShortStringUTF8(data));
    }

    @Test
    public void testIndexes_3Parts() {
        var outPacket = OutPacket.simpleBuffer();

        var binarSerialWrapper = new BinarSerialWrapper(outPacket, 3);
        outPacket.writeShortStringUTF8("abcdefghijklmnop");
        binarSerialWrapper.endPart(9);

        outPacket.writeShortStringUTF8("ABC");
        binarSerialWrapper.endPart(5);


        outPacket.writeShortStringUTF8("123456789");
        binarSerialWrapper.endPart(1);


        System.out.println(outPacket.getBuffer(true));

        var resp = readBinarLikeTheGame(outPacket.toByteArray());
        assertEquals(3, resp.size());
        assertTrue(resp.containsKey(9));
        var data = resp.get(9);
        assertEquals("abcdefghijklmnop", BufferUtils.getShortStringUTF8(data));
        assertTrue(resp.containsKey(5));
        data = resp.get(5);
        assertEquals("ABC", BufferUtils.getShortStringUTF8(data));
        assertTrue(resp.containsKey(1));
        data = resp.get(1);
        assertEquals("123456789", BufferUtils.getShortStringUTF8(data));
    }

    public Map<Integer, ByteBuffer> readBinarLikeTheGame(byte[] binarBuild) {
        ByteBuffer buffer = ByteBuffer.wrap(binarBuild);
        int tocLength = buffer.get();
        byte[] tocIndex = new byte[tocLength];
        int[] tocOffset = new int[tocLength];

        var res = new HashMap<Integer, ByteBuffer>(tocLength);
        int i;
        for(i = 0; i < tocLength; ++i) {
            tocIndex[i] = buffer.get();
            tocOffset[i] = buffer.getInt();
        }

        for(i = 0; i < tocLength; ++i) {
            int index = tocIndex[i];
            int offset = tocOffset[i];
            int size;
            if (i < tocLength - 1) {
                size = tocOffset[i + 1] - offset - 1;
            } else {
                size = buffer.limit() - offset - 1;
            }
            System.out.println("partIndex: " + index + ", off: " + offset + ", size: " + size);

            var partData = Unpooled.buffer(size, size);
            buffer.position(offset + 1);
            buffer.get(partData.array(), 0, size);
            System.out.println("\t Data: " + BufferUtils.toString(partData.array(), true));
            res.put(index, ByteBuffer.wrap(partData.array()));
        }
        return res;
    }
}
