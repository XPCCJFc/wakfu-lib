package wakfulib.logger.impl;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wakfulib.utils.BufferUtils;

import java.nio.ByteBuffer;

public class WakfulibLogAdapter extends Slf4jWakfuLibLogAdapter {

    public static WakfulibLogAdapter getLogger(Class<?> clazz) {
        return new WakfulibLogAdapter(LoggerFactory.getLogger(clazz));
    }

    public static WakfulibLogAdapter getLogger(String name) {
        return new WakfulibLogAdapter(LoggerFactory.getLogger(name));
    }

    public static WakfulibLogAdapter getLogger(Class<?> clazz, String name) {
        return new WakfulibLogAdapter(LoggerFactory.getLogger(clazz.getName() + name));
    }

    public WakfulibLogAdapter(Logger logger) {
        super(logger);
    }

    @Override
    public void log(ByteBuffer original, boolean fromServer) {
        if (isTraceEnabled()) {
            trace(BufferUtils.toString(BufferUtils.toArray(original), true));
        }
    }

    @Override
    public void log(ByteBuf copy, boolean fromServer) {
        if (isTraceEnabled()) {
            var array = BufferUtils.toArray(copy);
            trace("HEX DUMP of size (" + array.length + ")\n" + BufferUtils.toString(array, true));
        }
    }

    @Override
    public void logEdited(ByteBuf copy, boolean b) {

    }

    @Override
    public void logSSlHS(boolean b) {

    }
}
