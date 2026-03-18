package wakfulib.ui.proxy.view.scripting;

import static wakfulib.ui.tv.porst.swingx.JXCollapsiblePane.collapsiblePaneWithTitle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.model.def.scripting.ScriptParsingException;
import wakfulib.ui.proxy.model.def.scripting.ScriptingModel;
import wakfulib.ui.proxy.model.def.scripting.generation.GeneratedClass;
import wakfulib.ui.proxy.model.def.scripting.generation.ToClientGeneratedClass;
import wakfulib.ui.proxy.model.def.scripting.generation.ToServerGeneratedClass;
import wakfulib.ui.proxy.model.def.scripting.instructions.SimpleScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.storage.CustomTypesRegistry;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.ui.proxy.view.packetview.HexPacketView;
import wakfulib.ui.proxy.view.packetview.impl.ExternalPacketView;
import wakfulib.ui.proxy.view.packetview.listeners.JTreeHexViewMouseListener;
import wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener;
import wakfulib.ui.tv.porst.jhexview.IHexViewHoverListener;
import wakfulib.ui.tv.porst.splib.gui.tree.IconNode;
import wakfulib.ui.tv.porst.splib.gui.tree.WakfuLibTreeCellRenderer;
import wakfulib.ui.tv.porst.swingx.JXCollapsiblePane;
import wakfulib.ui.tv.porst.swingx.JXCollapsiblePane.Direction;
import wakfulib.ui.tv.porst.swingx.VerticalLayout;
import wakfulib.ui.utils.ExceptionDialog;
import wakfulib.ui.utils.IconUtils;
import wakfulib.ui.utils.TextLineNumber;
import wakfulib.utils.StringUtils;

@Slf4j
public class ScriptingPanel extends JFrame {
    private final JTextPane scriptingArea;
    private final IconNode root;
    private final JTree tree;
    private final TextLineNumber lineNumberUtil;
    private final JLabel bufferInfo;
    private final JButton runButton;
    @Getter
    private HexPacketView packetView;
    private ScriptingModel scriptingModel;
    private ByteBuffer lastBuffer;
    private ByteOrder ordering = null;

    public ScriptingPanel() {
        super("Scripting");
        var appIcon = SnifferWindow.getAppIcon(false);
        if (appIcon != null) {
            setIconImage(appIcon);
        }
        setAlwaysOnTop(true);
        scriptingModel = new ScriptingModel();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(500, 500));
        setLayout(new BorderLayout());
        scriptingArea = new JTextPane(new ScriptingStyledDocument(getKeyWords()));
        scriptingArea.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        Options options = Settings.getInstance().getOptions();
        if (StringUtils.isTrimmedNotEmpty(options.getLastScript())) {
            scriptingArea.setText(options.getLastScript());
        }

        var MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        final UndoManager undoManager = new UndoManager();
        Document doc = scriptingArea.getDocument();
        doc.addUndoableEditListener(e -> {
            var edit = e.getEdit();
            //This is fking stupid but mandatory due to a regression in jdk9,10,11...
            // see http://fritzthecat-blog.blogspot.com/2020/02/java-11-swing-exception-when-casting.html
            AbstractDocument.DefaultDocumentEvent realEdit;
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
                realEdit = (AbstractDocument.DefaultDocumentEvent) edit;
            } else {    // workaround above Java 1.8
                try {
                    Field ddeField = edit.getClass().getDeclaredField("dde");
                    ddeField.setAccessible(true);
                    Object dde = ddeField.get(edit);
                    realEdit = (AbstractDocument.DefaultDocumentEvent) dde;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            if (realEdit.getType() != EventType.CHANGE) {
                undoManager.addEdit(realEdit);
            }
        });
        scriptingArea.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } catch (CannotUndoException e) {
                    log.error("Cannot undo !", e);
                }
            }
        });
        scriptingArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK), "Undo");
        scriptingArea.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } catch (CannotRedoException e) {
                    log.error("Cannot redo !", e);
                }
            }
        });
        scriptingArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, MASK), "Redo");

        JPanel controlButtons = new JPanel();
        controlButtons.setLayout(new BoxLayout(controlButtons, BoxLayout.X_AXIS));

        var clearButton = new JButton();
        clearButton.setToolTipText("Clear");
        clearButton.setIcon(IconUtils.loadIcon("/icons/trash.png"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scriptingArea.setText(null);
                scriptingModel.clear();
                root.setUserObject("Script");
                root.removeAllChildren();
                ((DefaultTreeModel) tree.getModel()).reload(root);
                tree.setBackground(Color.WHITE);
            }
        });

        var generateCodeButton = new JButton(new AbstractAction("GenerateCode") {
            @Override
            public void actionPerformed(ActionEvent e) {
                var instructions = scriptingModel.getInstructions();
                var size = instructions.size();
                boolean autodetectToServer = false;
                if (size >= 2) {
                    var instru1 = instructions.get(0);
                    var instru2 = instructions.get(1);
                    if (instru1 instanceof SimpleScriptingInstruction s1 && instru2 instanceof SimpleScriptingInstruction s2) {
                        autodetectToServer = s1.getLabel().trim().equals("size") && s2.getLabel().trim().equals("archTarget");
                    }
                }

//                var res = JOptionPane.showOptionDialog(ScriptingPanel.this, "What is type of the generated message ?", "Type of message",
//                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Server", "Client"}, "Server");
//                if (res != JOptionPane.CLOSED_OPTION) {

                GeneratedClass generator;
                if (autodetectToServer) {
                    generator = new ToServerGeneratedClass("MessageXX", 9999,
                        instructions.size() <= 3 ? Collections.emptyList() : instructions.subList(3, instructions.size()), (byte) 1, "packageName",
                        "Version");
                } else {
                    generator = new ToClientGeneratedClass("MessageXX", 9999,
                        instructions.size() <= 2 ? Collections.emptyList() : instructions.subList(2, instructions.size()), "packageName", "Version");
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(generator.generateCode()), null);
//                }
            }
        });

        generateCodeButton.setEnabled(false);

        var parseButton = new JButton();
        parseButton.setToolTipText("Parse");
        parseButton.setIcon(IconUtils.loadIcon("/icons/compile.png"));
        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tree.setBackground(Color.WHITE);
                DefaultMutableTreeNode rootNode = root;
                rootNode.removeAllChildren();
                Options options = Settings.getInstance().getOptions();
                if (options.isSAVE_LAST_SCRIPT() && StringUtils.isTrimmedNotEmpty(scriptingArea.getText())) {
                    options.setLastScript(scriptingArea.getText());
                }
                try {
                    scriptingModel.parse(scriptingArea.getText());
                    for (var instruction : scriptingModel.getInstructions()) {
                        rootNode.add(instruction.toNode());
                    }
                    expandAllNodes();
                    lineNumberUtil.resetError();
                    lineNumberUtil.repaint();
                    generateCodeButton.setEnabled(true);
                } catch (ScriptParsingException e) {
                    generateCodeButton.setEnabled(false);
                    lineNumberUtil.setErrorLine(e.getLine());
                    lineNumberUtil.repaint();
                    JOptionPane.showMessageDialog(ScriptingPanel.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    root.removeAllChildren();
                } finally {
                    ((DefaultTreeModel) tree.getModel()).reload();
                }
            }
        });

        runButton = new JButton();
        runButton.setToolTipText("Run");
        runButton.setIcon(IconUtils.loadIcon("/icons/execute.png"));

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (packetView.getSelectedValue() != null) {
                    var result = packetView.analyse(root.children(), ordering);
                    var buffer = result.getBuffer();
                    if (result.getException() == null) {
                        if (buffer.remaining() == 0) {
                            root.setIcon(null);
                            tree.setBackground(new Color(223, 253, 223));
                        } else {
                            root.setIcon(ExternalPacketView.WARN_ICON);
                            tree.setBackground(new Color(255, 236, 210));
                        }
                    } else {
                        log.error("!", result.getException());
                        new ExceptionDialog(result.getException()).setVisible(ScriptingPanel.this);
                        tree.setBackground(new Color(255, 198, 198));
                        if (result.getLastNode() != null) {
                            result.getLastNode().setIcon(ExternalPacketView.ERR_ICON);
                            root.setIcon(ExternalPacketView.ERR_ICON);
                        }
                    }
                    lastBuffer = buffer;
                    ScriptingPanel.this.updateBufferInfo();
                    root.setUserObject(
                        "Pos: " + buffer.position() + ", Lim: " + buffer.limit() + ", Remaining: " + (buffer.limit() - buffer.position()));
                    ((DefaultTreeModel) tree.getModel()).reload();
                } else {
                    JOptionPane.showMessageDialog(ScriptingPanel.this, "No select packet in the packet view", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        runButton.setEnabled(false);

        var duplicateButton = new JButton();
        duplicateButton.setToolTipText("Duplicate this scripting panel");
        duplicateButton.setIcon(IconUtils.loadIcon("/icons/frame.png"));
        duplicateButton.addActionListener(e -> {
            ScriptingPanel scriptingPanel = new ScriptingPanel();
            scriptingPanel.setPacketView((packetView));
            scriptingPanel.scriptingModel = scriptingModel.copy();
            scriptingPanel.scriptingArea.setText(scriptingArea.getText());
            var location = getLocation();
            scriptingPanel.setLocation(location.x + getWidth() + 3, location.y);
            scriptingPanel.setVisible(true);
        });
        ordering = ("LITTLE_ENDIAN".equals(Settings.getInstance().getOptions().getLastScriptEndianess()) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        JButton orderingButton = new JButton("Current ordering: " + ordering);
        orderingButton.addActionListener(a -> {
            ordering = ordering.toString().equals("BIG_ENDIAN") ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            orderingButton.setText("Current ordering: " + ordering);
            options.setLastScriptEndianess(ordering.toString());
        });

        controlButtons.add(duplicateButton);
        controlButtons.add(clearButton);
        controlButtons.add(parseButton);
        controlButtons.add(this.runButton);
        controlButtons.add(orderingButton);
        controlButtons.add(generateCodeButton);

        add(controlButtons, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setLayout(new VerticalLayout());
        JXCollapsiblePane collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Headers");
        collapsiblePane.setDelta(2);
        collapsiblePane.add(button("Client Header", "INT size\nBYTE archTarget\nSHORT opcode\n"));
        collapsiblePane.add(button("Server Header", "INT size\nSHORT opcode\n"));

        collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Common Types");
        collapsiblePane.setDelta(2);
        collapsiblePane.add(button("Boolean", "BOOLEAN "));
        collapsiblePane.add(button("Byte", "BYTE "));
        collapsiblePane.add(button("Short", "SHORT "));
        collapsiblePane.add(button("Integer", "INT "));
        collapsiblePane.add(button("Long", "LONG "));
        collapsiblePane.add(button("Double", "DOUBLE "));
        collapsiblePane.add(button("Float", "FLOAT "));

        collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Stack Aware Types");
        collapsiblePane.add(button("Byte String", "bSTRING "));
        collapsiblePane.add(button("Short String", "sSTRING "));
        collapsiblePane.add(button("Integer String", "iSTRING "));
        collapsiblePane.add(button("c String", "cSTRING "));
        collapsiblePane.add(button("Remaining String", "rSTRING "));
        collapsiblePane.add(button("Stack String", "pSTRING "));
        collapsiblePane.add(button("Short Buffer", "sBUFFER "));
        collapsiblePane.add(button("Integer Buffer", "iBUFFER "));
        collapsiblePane.add(button("Remaining Buffer", "rBUFFER "));
        collapsiblePane.add(button("Stack Buffer", "pBUFFER "));

        collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Loops");
        collapsiblePane.add(button("Loop", "LOOP "));
        collapsiblePane.add(button("REMANINGLOOP", "REMANINGLOOP\n"));
        collapsiblePane.add(button("PEEKLOOP", "PEEKLOOP\n"));
        collapsiblePane.add(button("IF", "IF\n"));
        collapsiblePane.add(button("STRUCT", "STRUCT\n"));
        collapsiblePane.add(button("End", "END\n"));

        collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Misc Types");
        collapsiblePane.add(button("Skip <expr>", "SKIP "));
        collapsiblePane.add(button("Proto <name>", "PROTO "));
        collapsiblePane.add(button("X Buffer <expr>", "xBUFFER "));
        collapsiblePane.add(button("X String <expr>", "xSTRING "));
        collapsiblePane.add(button("Enum <size> <name>", "idEnum "));

        collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Endianess");
        collapsiblePane.add(button("Big Endian", "BIG_ENDIAN\n"));
        collapsiblePane.add(button("Little Endian", "LITTLE_ENDIAN\n"));

        var iterator = CustomTypesRegistry.getAllCustomTypesWithLabel().iterator();
        if (iterator.hasNext()) {
            collapsiblePane = collapsiblePaneWithTitle(buttons, Direction.UP, "Misc Types");
            do {
                var next = iterator.next();
                collapsiblePane.add(button(next.getKey(), next.getValue().getName() + " "));
            } while (iterator.hasNext());
        }

        JScrollPane scrollPane = new JScrollPane(buttons, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(7);
        add(scrollPane, BorderLayout.WEST);

        JPanel result = new JPanel(new BorderLayout());
        root = new IconNode("Script");
        tree = new JTree(root);
        tree.setCellRenderer(new WakfuLibTreeCellRenderer());
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setToggleClickCount(1);
        bufferInfo = new JLabel();
        result.add(bufferInfo, BorderLayout.NORTH);
        result.add(new JScrollPane(tree), BorderLayout.CENTER);
        var codePane = new JScrollPane(scriptingArea);
        lineNumberUtil = new TextLineNumber(scriptingArea, 2);
        lineNumberUtil.setCurrentLineForeground(Color.BLACK);
        lineNumberUtil.setForeground(new Color(98, 97, 97));

        codePane.setRowHeaderView(lineNumberUtil);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codePane, result);
        add(splitPane, BorderLayout.CENTER);
        pack();
        splitPane.setDividerLocation(0.5);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var scriptingPanel = new ScriptingPanel();
            scriptingPanel.setVisible(true);
        });
    }

    private Set<String> getKeyWords() {
        var keywords = new HashSet<>(ScriptingModel.getAllTypes().keySet());
        keywords.add("SKIP");
        keywords.add("LOOP");
        keywords.add("REMANINGLOOP");
        keywords.add("PEEKLOOP");
        keywords.add("END");
        keywords.add("IF");
        keywords.add("STRUCT");
        return keywords;
    }

    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private JButton button(String label, String toWrite) {
        JButton button = new JButton(label);
        button.addActionListener(a -> {
            var newText = toWrite;
            try {
                if (scriptingArea.getSelectionEnd() == scriptingArea.getSelectionStart()) {
                    if (scriptingArea.getSelectionStart() > 0 &&
                        scriptingArea.getText(scriptingArea.getSelectionStart(), 1).equals("\n") &&
                        !scriptingArea.getText(scriptingArea.getSelectionStart() - 1, 1).equals("\n")) {
                        newText = "\n" + newText;
                    }
                }
            } catch (Exception ignored) {
            }
            scriptingArea.replaceSelection(newText);
            scriptingArea.requestFocus();
        });
        return button;
    }

    public void setPacketView(HexPacketView packetView) {
        this.packetView = packetView;
        runButton.setEnabled(true);
        IHexViewHoverListener listener = new TreeHexViewHoverListener(tree, packetView.getView());
        var viewMouseListener = new JTreeHexViewMouseListener(tree, packetView.getView()) {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null && lastBuffer != null) {
                    Object lastPathComponent = path.getLastPathComponent();
                    if (lastPathComponent instanceof PacketDefinitionNode component) {
                        if (component.getParent() == null) {
                            updateBufferInfo();
                        } else {
                            var end = component.getOffset() + component.getSize();
                            bufferInfo.setText("Pos: " + component.getOffset() + ", Lim: " + end + ", Remaining: " + (lastBuffer.limit() - end));
                        }
                    }
                }
            }
        };
        tree.addMouseListener(viewMouseListener);
        tree.addTreeSelectionListener(viewMouseListener);
        packetView.getView().addHoverListener(listener);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                HexPacketView packetView = getPacketView();
                if (packetView != null) {
                    packetView.removeHexHoverListener(listener);
                }
            }
        });
    }

    private void updateBufferInfo() {
        if (lastBuffer != null) {
            bufferInfo.setText(
                "Pos: " + lastBuffer.position() + ", Lim: " + lastBuffer.limit() + ", Remaining: " + (lastBuffer.limit() - lastBuffer.position()));
        } else {
            bufferInfo.setText("No info");
        }
    }
}
