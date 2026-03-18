package wakfulib.ui.utils;

import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class LoadingPopup extends JDialog {

    public LoadingPopup(Frame owner, String title) {
        super(owner, title);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());
        JProgressBar comp = new JProgressBar();
        add(comp);
        setAlwaysOnTop(true);
        comp.setIndeterminate(true);
        pack();
        setVisible(false);
    }
}
