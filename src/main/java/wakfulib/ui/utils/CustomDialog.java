package wakfulib.ui.utils;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Predicate;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import lombok.Setter;

public class CustomDialog extends JDialog implements ActionListener, PropertyChangeListener {

    public static final Predicate<String> INT_VALIDATOR = s -> {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };


    private String typedText = null;
    private final JTextField textField;

    private final JOptionPane optionPane;

    private final String btnString1 = "Enter";
    private final String btnString2 = "Cancel";
    @Setter
    private Predicate<String> validator;

    /**
     * Returns null if the typed string was invalid;
     * otherwise, returns the string as the user entered it.
     */
    public String getValidatedText() {
        return typedText;
    }

    /** Creates the reusable dialog. */
    public CustomDialog(Frame aFrame) {
        super(aFrame, true);

        textField = new JTextField(10);
        setPreferredSize(new Dimension(350, 200));
        setSize(new Dimension(350, 150));

        //Create an array of the text and components to be displayed.
        Object[] array = {"Enter a new value :", textField};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        //Register an event handler that puts the text into the option pane.
        textField.addActionListener(this);

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                typedText = textField.getText();
                if (validator.test(typedText)) {
                    //we're done; clear and dismiss the dialog
                    clearAndHide();
                } else {
                    //text was invalid
                    textField.selectAll();
                    JOptionPane.showMessageDialog(
                            CustomDialog.this,
                            "Sorry, \"" + typedText + "\" "
                                    + "is not a valid value for this option.\n",
                            "Invalid value",
                            JOptionPane.ERROR_MESSAGE);
                    typedText = null;
                    textField.requestFocusInWindow();
                }
            } else { //user closed dialog or clicked cancel
                typedText = null;
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        textField.setText(null);
        setVisible(false);
    }

    public void setOption(String text, String optionName) {
        setTitle("Change option '" + optionName + "' value");
        textField.setText(text);
    }
}
