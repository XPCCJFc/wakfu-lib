package wakfulib.ui.proxy.io;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import lombok.Setter;

public class InBatchLatchedParameter<RES> extends InBatchParameter<RES> {

    @Setter
    protected CountDownLatch countDownLatch;

    public InBatchLatchedParameter(File file) {
        super(file);
    }

    @Override
    public void done() {
        countDownLatch.countDown();
    }
}
