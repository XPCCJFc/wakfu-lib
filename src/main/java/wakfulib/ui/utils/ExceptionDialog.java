package wakfulib.ui.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 *
 * ExceptionDialog.
 *
 * Displays an exception stack trace in a panel south of the main dialog area.
 *
 *
 * @author Oliver Watkins (c)
 */
public class ExceptionDialog extends JDialog {

    private int dialogWidth = 500;
    private int dialogHeight = 140;

    private JLabel iconLabel = new JLabel();

    // is error panel opened up
    private boolean open = false;

    private JLabel errorLabel = new JLabel();
    private JTextArea errorTextArea = new JTextArea("");

    private JTextArea exceptionTextArea = new JTextArea("");
    private JScrollPane exceptionTextAreaSP;

    private JButton okButton = new JButton("OK");
    private JButton viewButton = new JButton("View Error");

    private JPanel topPanel = new JPanel(new BorderLayout());

    public ExceptionDialog(String errorLabelText, String errorDescription,
                           Throwable e) {
        if (errorLabelText == null) {
            if (e.getMessage() != null) {
                errorLabelText = e.getMessage();
            } else {
                errorLabelText = e.getClass().getSimpleName();
            }
        }

        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        setSize(dialogWidth, dialogHeight);

//        setResizable(false);

        errorTextArea.setText(errorDescription);

        iconLabel.setText(errorLabelText);

        exceptionTextArea.setText(errors.toString());

        exceptionTextAreaSP = new JScrollPane(exceptionTextArea);

        iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        getOwner().setIconImage(iconToImage(UIManager.getIcon("OptionPane.errorIcon")));
        setupUI();
        setUpListeners();
    }

    public ExceptionDialog(String errorLabelText, Throwable e) {
        this(errorLabelText, null, e);
    }

    public ExceptionDialog(Throwable e) {
        this(null, null, e);
    }
    
    public void setVisible(Component parent) {
        setLocationRelativeTo(parent);
        super.setVisible(true);
    }

    public void setupUI() {
        this.setTitle("Error");

        errorTextArea.setLineWrap(false);
        errorTextArea.setWrapStyleWord(false);
        errorTextArea.setEditable(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.add(okButton);
        buttonPanel.add(viewButton);

        errorTextArea.setBackground(iconLabel.getBackground());

        exceptionTextAreaSP.setBorder(
            new CompoundBorder(new EmptyBorder(new Insets(5, 5, 5, 5)),
                new LineBorder(Color.BLACK, 1)));

        exceptionTextArea.setPreferredSize(new Dimension(100, 100));

        this.add(iconLabel, BorderLayout.NORTH);
        this.add(topPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setUpListeners() {

        okButton.addActionListener(e -> ExceptionDialog.this.setVisible(false));

        viewButton.addActionListener(e -> {

            if (open) {
                viewButton.setText("View Error");

                topPanel.remove(exceptionTextAreaSP);

                ExceptionDialog.this.setSize(dialogWidth, dialogHeight);

                topPanel.revalidate();

                open = false;

            } else {

                viewButton.setText("Hide Error");

                topPanel.add(exceptionTextAreaSP, BorderLayout.CENTER);

                ExceptionDialog.this.setSize(dialogWidth,
                    dialogHeight + 100);

                topPanel.revalidate();

                open = true;
            }
        });
    }

    static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        }
        else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }
}
