package wakfulib.ui.proxy.io;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InBatchParameter<RES> {
    @Getter
    private final File file;

    public void done() {

    }

    public void process(RES res) {

    }
}
