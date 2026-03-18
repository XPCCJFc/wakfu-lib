package test;

import org.junit.Assert;
import org.junit.Test;
import wakfulib.logic.OutPacket;

public class OutPacketTests {

    @Test
    public void newPacket() {
        var outPacket = new OutPacket((byte) 1, 99);
        Assert.assertEquals("00 00 01 00 63", outPacket.getBuffer(true));
        outPacket = new OutPacket((byte) 1, 98);
        outPacket.finish();
        Assert.assertEquals("00 05 01 00 62", outPacket.getBuffer(true));

        outPacket = new OutPacket(false, 97);
        Assert.assertEquals("00 00 00 61", outPacket.getBuffer(true));
        outPacket = new OutPacket(false, 96);
        outPacket.finish();
        Assert.assertEquals("00 04 00 60", outPacket.getBuffer(true));

        try {
            new OutPacket(false, -1);
            Assert.fail("Packet id of -1 should have failed !");
        } catch (Exception ignored) { }

        try {
            new OutPacket((byte) 1, -1);
            Assert.fail("Packet id of -1 should have failed !");
        } catch (Exception ignored) { }
    }

    @Test
    public void toByteArray() {
        Assert.assertArrayEquals(new byte[] {0, 0, 1, 0, 99}, new OutPacket((byte) 1, 99).toByteArray());

        Assert.assertArrayEquals(new byte[] {0, 0, 0, 99}, new OutPacket(false, 99).toByteArray());
        Assert.assertArrayEquals(new byte[] {}, OutPacket.simpleBuffer().toByteArray());

        var outPacket = new OutPacket(false, 99);
        outPacket.finish();
        Assert.assertArrayEquals(new byte[] {0, 4, 0, 99}, outPacket.toByteArray());
    }

    @Test
    public void testMarksNotCompleted() {
        var outPacket = new OutPacket(false, 16);
        outPacket.markShort();
        assertExceptionOnFinish(outPacket);

        outPacket = new OutPacket(false, 16);
        outPacket.markByte();
        assertExceptionOnFinish(outPacket);

        outPacket = new OutPacket(false, 16);
        outPacket.markInt();
        assertExceptionOnFinish(outPacket);



        outPacket = new OutPacket(false, 16);
        outPacket.markShort();
        outPacket.endMarkShort();
        outPacket.markInt();
        assertExceptionOnFinish(outPacket);

        outPacket = new OutPacket(false, 16);
        outPacket.markShort();
        outPacket.markInt();
        outPacket.endMarkShort();
        assertExceptionOnFinish(outPacket);
    }

    @Test
    public void testMarksCompleted() {
        var outPacket = new OutPacket(false, 16);
        outPacket.markByte();
        outPacket.writeIntStringUTF8("testt");
        outPacket.endMarkByte();
        outPacket.finish();
        Assert.assertEquals("00 0E 00 10 " + "09 " + "00 00 00 05 " + "74 65 73 74 74", outPacket.getBuffer(true));

        outPacket = new OutPacket(false, 16);
        outPacket.markShort();
        outPacket.writeIntStringUTF8("testt");
        outPacket.endMarkShort();
        outPacket.finish();
        Assert.assertEquals("00 0F 00 10 " + "00 09 " + "00 00 00 05 " + "74 65 73 74 74", outPacket.getBuffer(true));

        outPacket = new OutPacket(false, 16);
        outPacket.markInt();
        outPacket.writeIntStringUTF8("testt");
        outPacket.endMarkInt();
        outPacket.finish();
        Assert.assertEquals("00 11 00 10 " + "00 00 00 09 " + "00 00 00 05 " + "74 65 73 74 " + System.lineSeparator() + "74", outPacket.getBuffer(true));
    }

    private void assertExceptionOnFinish(OutPacket outPacket) {
        try {
            outPacket.finish();
            Assert.fail();
        } catch (Exception e) {}
    }
}
