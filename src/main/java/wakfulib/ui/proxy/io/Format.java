package wakfulib.ui.proxy.io;

import io.netty.buffer.Unpooled;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.internal.Version;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.model.FakePacket;
import wakfulib.ui.proxy.model.Packet;
import wakfulib.ui.proxy.model.WakfuPacket;

@Slf4j
public enum Format {

    WAKFU_DUMP("wkfdump") {
        @Override
        public List<WakfuPacket> readInternal(InputStream in, File file) throws Exception {
            ArrayList<WakfuPacket> res = new ArrayList<>();
            BufferedReader logFile = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = logFile.readLine()) != null) {
                String[] split = line.split(":");
                boolean fromServ = (Integer.parseInt(split[0]) == 1);
                List<Byte> collect = Arrays.stream(split)
                    .skip(1)
                    .map(Byte::parseByte)
                    .toList();
                byte[] data = new byte[collect.size()];
                for (int i = 0; i < collect.size(); i++) {
                    data[i] = collect.get(i);
                }
                res.add(new WakfuPacket(Unpooled.wrappedBuffer(data), fromServ));
            }
            return res;
        }
    },
    COMPRESSED_WAKFU_DUMP_2("cwkfdump2") {
        @Override
        public List<WakfuPacket> readInternal(InputStream in, File file) throws Exception {
            try {
                DataInputStream data = new DataInputStream(new GZIPInputStream(in));
                List<WakfuPacket> temp = new ArrayList<>();
                while (in.available() > 0) {
                    boolean fromServer = data.readBoolean();
                    LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(data.readLong()), ZoneOffset.UTC);
                    byte[] packetData = new byte[data.readInt()];
                    data.readFully(packetData);
                    temp.add(new WakfuPacket(time, Unpooled.wrappedBuffer(packetData), fromServer));
                }
                data.close();
                return temp;
            } catch (ZipException zipException) {
                log.warn("Zip not detected, opening normally");
                return WAKFU_DUMP_2.read(file);
            }
        }
    },
    SNOUFLE_DATA("snfldump") {
        @Override
        public List<WakfuPacket> readInternal(InputStream in, File file) throws Exception {
            DataInputStream data = new DataInputStream(in);
            byte snoofleVersion = data.readByte();
            Version version = Version.unserialize(data);
            ArrayList<WakfuPacket> res = new ArrayList<>();
            while (in.available() > 0) {
                boolean fromServer = data.readBoolean();
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.readLong()), ZoneOffset.UTC);
                byte[] packetData = new byte[data.readInt()];
                data.readFully(packetData);
                res.add(new WakfuPacket(time, Unpooled.wrappedBuffer(packetData), fromServer));
            }
            ReadersFlags.version = version;
            ReadersFlags.snoufleVersion = snoofleVersion;
            return res;
        }

        static final byte outSnoofleVersion = 2;

        @Override
        public void write(DataOutputStream out, List<Packet> packets) throws Exception {
            out.writeByte(outSnoofleVersion);
            Version.getCurrent().serialize(out);
            for (Packet packet : packets) {
                if (packet instanceof WakfuPacket wakfuPacket) {
                    out.writeBoolean(wakfuPacket.isFromServer());
                    out.writeLong(wakfuPacket.getTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                    byte[] data = wakfuPacket.getData();
                    out.writeInt(data.length);
                    out.write(data);
                }
            }
        }
    },
    HEXDUMP("hex", false) {

        private final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

        @Override
        public List<WakfuPacket> readInternal(InputStream in, File file) throws Exception {
            var bytes = HexFormat.of().parseHex(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return Collections.singletonList(new WakfuPacket(LocalDateTime.now(), Unpooled.wrappedBuffer(bytes), false));
        }

        @Override
        public void write(DataOutputStream out, List<Packet> packets) throws Exception {
            final char[] lineSep = System.lineSeparator().toCharArray();
            for (Packet packet : packets) {
                if (packet instanceof WakfuPacket wakfuPacket) {
                    byte[] bytes = wakfuPacket.getData();
                    char[] hexChars = new char[(bytes.length * 2) + 1 + lineSep.length];
                    hexChars[0] = (wakfuPacket.isFromServer() ? '<' : '>');
                    for ( int j = 0; j < bytes.length; j++ ) {
                        int v = bytes[j] & 0xFF;
                        hexChars[j * 2 + 1] = HEX_ARRAY[v >>> 4];
                        hexChars[j * 2 + 2] = HEX_ARRAY[v & 0x0F];
                    }
                    System.arraycopy(lineSep, 0, hexChars, hexChars.length - lineSep.length, lineSep.length);
                    out.writeBytes(new String(hexChars));
                }
            }
        }
    },
    WAKFU_DUMP_2("wkfdump2") {
        @Override
        public List<WakfuPacket> readInternal(InputStream in, File file) throws Exception {
            ArrayList<WakfuPacket> res = new ArrayList<>();
            DataInputStream data = new DataInputStream(in);
            while (in.available() > 0) {
                boolean fromServer = data.readBoolean();
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(data.readLong()), ZoneOffset.UTC);
                byte[] packetData = new byte[data.readInt()];
                data.readFully(packetData);
                res.add(new WakfuPacket(time, Unpooled.wrappedBuffer(packetData), fromServer));
            }
            return res;
        }
    };
    private static final Format[] VALUES = values();
    public static final String[] READABLE_EXTENSIONS = Arrays.stream(VALUES)
            .filter(Format::isReadingSupported)
            .map(v -> v.extension)
            .toArray(String[]::new);

    @Getter
    private final String extension;
    @Getter
    private final boolean readingSupported;

    Format(String extension) {
        this(extension, true);
    }

    Format(String extension, boolean readingSupported) {
        this.extension = extension;
        this.readingSupported = readingSupported;
    }

    public static Format getByName(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            if (fileName.length() == i) return null;
            String extension = fileName.substring(i + 1);
            for (Format value : VALUES) {
                if (value.extension.equals(extension)) {
                    return value;
                }
            }
        }
        return null;
    }

    public List<WakfuPacket> read(File file) throws Exception {
        resetFlags();
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return readInternal(in, file);
        }
    }

    public static List<? extends DataPacket> readAll(File fromFile) throws Exception {
      if (! fromFile.exists()) return Collections.emptyList();
      String path = fromFile.getPath();
      Format format = Format.getByName(path);
      if (format == null) {
          var fakePacket = new FakePacket(Files.readAllBytes(fromFile.toPath()), false);
          fakePacket.setName(fromFile.getName());
          return Collections.singletonList(fakePacket);
//        throw new IllegalArgumentException("Unknown format for file : " + path);
      }
        var red = format.read(fromFile);
        red.forEach(w -> w.setFile(fromFile.getAbsolutePath()));
        return red;
    }

    public abstract List<WakfuPacket> readInternal(InputStream in, File file) throws Exception;

    private void resetFlags() {
        ReadersFlags.version = Version.UNKNOWN;
        ReadersFlags.snoufleVersion = -1;
    }

    public void write(DataOutputStream out, List<Packet> packets) throws Exception {
        throw new UnsupportedOperationException();
    }

    public static class ReadersFlags {
        @Getter
        private static byte snoufleVersion = -1;
        @Getter
        private static Version version = Version.UNKNOWN;
    }
}
