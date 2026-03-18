package wakfulib.ui.proxy.view.packetview;

import wakfulib.ui.tv.porst.jhexview.IHexViewHoverListener;
import wakfulib.ui.tv.porst.jhexview.JHexView;

public interface HexPacketView extends PacketView {
    void addHexHoverListener(IHexViewHoverListener listener);

    void removeHexHoverListener(IHexViewHoverListener listener);

    JHexView getView();
}
