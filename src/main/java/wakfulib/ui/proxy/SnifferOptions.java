package wakfulib.ui.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SnifferOptions {
    @Builder.Default
    private boolean sniffing = false;
    @Builder.Default
    private boolean hideAuth = false;
    @Builder.Default
    private boolean packetListEnabled = true;
//    private boolean internalScan = true;
}
