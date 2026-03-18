package wakfulib.utils.data;

import io.netty.buffer.ByteBuf;
import wakfulib.doc.NonNull;
import wakfulib.logic.OutPacket;

public final class BinarSerialWrapper {

    private final int binarIndex;
    private final ByteBuf internalData;
    private final int numberOfParts;
    @NonNull
    private final OutPacket outPacket;
    private int nextFinalIndex;

    private int lastIndex;

    public BinarSerialWrapper(@NonNull OutPacket outPacket, int numberOfParts) {
        this.outPacket = outPacket;
        if (numberOfParts < 0) throw new IllegalStateException("Negative number of parts doesn't make sense !");
        internalData = outPacket.getInternalBuffer();
        this.numberOfParts = numberOfParts;

        outPacket.writeByte(numberOfParts);
        binarIndex = internalData.writerIndex();
        for (int i = 0; i < numberOfParts; i++) {
            outPacket.writeByte(Byte.MIN_VALUE);
            outPacket.writeInt(Byte.MAX_VALUE);
        }
        lastIndex = internalData.writerIndex() - 1;
        nextFinalIndex = 0;
    }

    public void endPart(int partIndex) {
        if (nextFinalIndex >= numberOfParts) throw new IllegalStateException("Too much part !");
        internalData.setByte(binarIndex + (nextFinalIndex * 5), partIndex);
        internalData.setInt(binarIndex + (nextFinalIndex * 5) + 1, lastIndex);
        lastIndex = internalData.writerIndex();
        outPacket.write(Byte.MIN_VALUE);
        nextFinalIndex++;
    }

}
