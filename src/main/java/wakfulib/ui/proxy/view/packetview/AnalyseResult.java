package wakfulib.ui.proxy.view.packetview;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import wakfulib.doc.Nullable;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;

@Getter
@AllArgsConstructor
public class AnalyseResult {
    @Nullable
    private final Exception exception;
    private final PacketDefinitionNode lastNode;
    private final ByteBuffer buffer;
}
