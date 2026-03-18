package test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.buttology.lwjgl.dds.DDSFile;
import wakfulib.logic.OutPacket;
import wakfulib.utils.FileHelper;
import wakfulib.utils.StringUtils;

public class FntToTab {

    public static short getTokenValue(String line, String token) {
        int tokenBegin = line.indexOf(token);
        if (tokenBegin == -1) throw new IllegalArgumentException("No token '" + token + "' in string '" + line + "'");
        int i = 0;
        int beginIndex = tokenBegin + token.length() + 1;
        while (line.length() > beginIndex + i) {
            if ((line.charAt(beginIndex + i) + "").matches("[0-9-]")) {
                i++;
            } else {
                break;
            }
        }
        return Short.parseShort(line.substring(beginIndex, beginIndex + i));
    }

    public static class ReadDDSs {
        public static void main(String[] args) throws Exception {
            var old_ = new DDSFile(new File("C:\\Users\\Trombonesolo\\AppData\\Roaming\\Arena Returns Client\\game\\contents\\gui_save\\tahoma-plain-11000.DDS"));
            System.out.println("old ok");
            var new_c5 = new DDSFile(new File("C:\\Users\\Trombonesolo\\Desktop\\Arthur\\5_0.dds"));
            System.out.println("new ok");
        }
    }

    public static class Read {
        public static void main(String[] args) throws Exception {
            ByteBuffer wrap = ByteBuffer.wrap(FileHelper.readFile("C:\\Users\\Trombonesolo\\IdeaProjects\\ArenaReturnsContent\\gui\\gui\\theme\\fonts\\_tahoma-plain-11.tab_old"));
            TabData res = new TabData(wrap);
            System.out.println("original tab file");
            System.out.println(res);
        }
    }


    public static void main(String[] args) {
        try (var br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Trombonesolo\\IdeaProjects\\ArenaReturnsContent\\gui\\gui\\theme\\fonts\\tahoma-plain-11.fnt")))) {
            String line;
            var tabData = new TabData();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.replaceAll("\s+", " ").split(" ");
                if (tokens.length == 0 || tokens[0] == null) continue;
                switch (tokens[0]) {
                    case "chars": {
                        tabData.numberOfChar = getTokenValue(line, "count");
                        break;
                    }
                    case "char": {
                        tabData.registerChar(new CharData(
                            getTokenValue(line, "id"),
                            getTokenValue(line, "x"),
                            getTokenValue(line, "y"),
                            getTokenValue(line, "width"),
                            getTokenValue(line, "height"),
                            getTokenValue(line, "xoffset"),
                            getTokenValue(line, "xadvance")
                        ));
                        break;
                    }
                    case "common": {
                        tabData.cellHeight = getTokenValue(line, "lineHeight");
                        break;
                    }
                }
            }
            tabData.chars.add(new CharData((short)-1279, (short)99, (short)99, (short)5, (short)13, (short)0, (short)6));
            tabData.chars.add(new CharData((short)-1278, (short)105, (short)99, (short)5, (short)13, (short)0, (short)6));
            tabData.chars.add(new CharData((short)0, (short)1, (short)1, (short)1, (short)13, (short)0, (short)11));
            tabData.save().dumpBuffer("tahoma-plain-11.tab");

            System.out.println(tabData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CharData {
        short code;
        short x;
        short y;
        short w;
        short h;
        short l;
        short t;

        public CharData(ByteBuffer byteBuffer) {
            this.code = byteBuffer.getShort();
            byteBuffer.getShort();
            this.x = byteBuffer.getShort();
            this.y = byteBuffer.getShort();
            this.w = byteBuffer.getShort();
            this.h = byteBuffer.getShort();
            this.l = byteBuffer.getShort();
            this.t = byteBuffer.getShort();
        }

        public CharData(short code, short x, short y, short w, short h, short l, short t) {
            this.code = code;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.l = l;
            this.t = t;
            if (l < 0) {
                this.x = (short) (this.x - l);
                this.l = 0;
            }
        }

        public void save(OutPacket outPacket) {
            outPacket.writeShort(code);
            outPacket.writeShort(0);
            outPacket.writeShort(x);
            outPacket.writeShort(y);
            outPacket.writeShort(w);
            outPacket.writeShort(h);
            outPacket.writeShort(l);
            outPacket.writeShort(t);
        }

        @Override
        public String toString() {
            return this.code + " (" +x + "," +y + ") [" +w + "," + h +"] <" + l + "," + t + ">";
        }
    }

    private static class TabData {
        short numberOfChar;
        short minCharCode = Short.MAX_VALUE;
        short maxCharCode = Short.MIN_VALUE;
        short cellHeight;
        List<CharData> chars = new ArrayList<>();

        TabData() {

        }

        TabData(ByteBuffer byteBuffer) {
            byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.position(10);
            numberOfChar = byteBuffer.getShort();
            byteBuffer.getShort();
            minCharCode = byteBuffer.getShort();
            maxCharCode = byteBuffer.getShort();
            cellHeight = byteBuffer.getShort();
            for (int i = 0; i < numberOfChar; i++) {
                chars.add(new CharData(byteBuffer));
            }
        }

        public OutPacket save() {
            var outPacket = OutPacket.simpleBuffer();
            System.out.println(outPacket.getInternalBuffer().order());
            outPacket.writeBytes(StringUtils.toUTF8("FONTRASTAR"));

            outPacket.writeShort(numberOfChar);
            outPacket.writeShort(1);//numpage
            outPacket.writeShort(minCharCode);
            outPacket.writeShort(maxCharCode);
            outPacket.writeShort(cellHeight);
            for (CharData aChar : chars) {
                aChar.save(outPacket);
            }
            return outPacket;
        }

        public void registerChar(CharData charData) {
            if (charData.code < minCharCode) {
                minCharCode = charData.code;
            }
            if (charData.code > maxCharCode) {
                maxCharCode = charData.code;
            }
            chars.add(charData);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            chars.stream().sorted(Comparator.comparingInt(d -> d.code)).forEach(d -> sb.append(d.toString()).append("\n"));
            return sb.toString();
        }
    }
}
