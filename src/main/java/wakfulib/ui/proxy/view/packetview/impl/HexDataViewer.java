package wakfulib.ui.proxy.view.packetview.impl;

import java.nio.ByteBuffer;
import javax.swing.JLabel;
import wakfulib.ui.tv.porst.jhexview.JHexView;

public class HexDataViewer extends JLabel {

    private long lastStart = -1L, lastLength = -1L;

    public HexDataViewer(JHexView view) {
        setText("No data");
        view.addHexListener((start, length) -> {
            if (lastStart == start && lastLength == length) return;
            lastLength = length;
            lastStart = start;
            
            var data = view.getData();
            if (start == -1 || length == 0 || data == null) {
                setText("NoData");
                return;
            };

            if (length < 0) {
                start = start + length;
                length = -length;
            }

            var startNormalized = (int) (start - start % 2) / 2;
            var lengthNormalized = (int) (length + length % 2) / 2;

            try {
                var wrap = ByteBuffer.wrap(data.getData(startNormalized, lengthNormalized));
                byte b = wrap.get();
                var res = new StringBuilder().append("I: ").append(startNormalized).append(" L: ").append(lengthNormalized)
                    .append("\t byte: ").append(b)
                    .append(", bool: ").append(b == 1);
                if (lengthNormalized >= 2) {
                    res.append(", short: ").append(wrap.getShort(0));
                    if (lengthNormalized >= 4) {
                        res.append(", int: ").append(wrap.getInt(0));
                        res.append(", float: ").append(wrap.getFloat(0));
                        if (lengthNormalized >= 8) {
                            res.append(", long: ").append(wrap.getLong(0));
                            res.append(", double: ").append(wrap.getDouble(0));
                        }
                    }
                }
                setText(res.toString());
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
