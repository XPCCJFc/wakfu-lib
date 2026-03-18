package wakfulib.ui.proxy.view.packetList;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AllUpdateDocumentListener implements DocumentListener {

    private final Runnable runnable;

    AllUpdateDocumentListener(Runnable toDo) {
        this.runnable = toDo;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        runnable.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        runnable.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        runnable.run();
    }

}
