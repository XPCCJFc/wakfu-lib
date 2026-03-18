package wakfulib.ui.proxy.view.packetview.impl;

import static wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener.PACKET_DEFINITION_HIGHLIGHT_LEVEL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logic.OutPacket;
import wakfulib.exception.NotImplementedException;
import wakfulib.ui.proxy.SnifferWindow;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.model.FakePacket;
import wakfulib.ui.proxy.model.FileLinkable;
import wakfulib.ui.proxy.model.def.PacketDefinitionNode;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.ui.proxy.view.packetview.AnalyseResult;
import wakfulib.ui.proxy.view.packetview.HexPacketView;
import wakfulib.ui.proxy.view.packetview.listeners.ConditionalJComponentEnablingViewListener;
import wakfulib.ui.proxy.view.packetview.listeners.JComponentEnablingViewListener;
import wakfulib.ui.proxy.view.packetview.listeners.JTreeHexViewMouseListener;
import wakfulib.ui.proxy.view.packetview.listeners.PacketViewListener;
import wakfulib.ui.proxy.view.packetview.listeners.TreeHexViewHoverListener;
import wakfulib.ui.tv.porst.jhexview.IHexViewHoverListener;
import wakfulib.ui.tv.porst.jhexview.JHexView;
import wakfulib.ui.tv.porst.jhexview.SimpleDataProvider;
import wakfulib.ui.tv.porst.splib.gui.tree.IconNode;
import wakfulib.ui.tv.porst.splib.gui.tree.WakfuLibTreeCellRenderer;
import wakfulib.ui.utils.DebuggerTreePopulator;
import wakfulib.ui.utils.ExceptionDialog;
import wakfulib.ui.utils.IconUtils;

@Slf4j
public class ExternalPacketView implements HexPacketView {
    public static final Icon WARN_ICON;
    public static final Icon OK_ICON;
    public static final Icon ERR_ICON;

    static {
        WARN_ICON = IconUtils.loadIcon("/icons/warn.png");
        OK_ICON = IconUtils.loadIcon("/icons/success.png");
        ERR_ICON = IconUtils.loadIcon("/icons/err.png");
    }

    @Getter
    private final JHexView view;
    private final JPanel mainPanel;
    private final IconNode topNode;

    private final JTree messageStructure;
    private final JButton viewFile;
    private final JButton debug;
    private String lastPacketName;
    @Getter
    private DataPacket selectedValue;

    private final List<PacketViewListener> viewListeners;

    public ExternalPacketView(SnifferWindow snifferWindow) {
        viewListeners = new ArrayList<>();
        lastPacketName = "";

        mainPanel = new JPanel();
        view = new JHexView();
        view.setEditable(false);
        view.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        File file = files.get(0);
                        if (snifferWindow.getSnifferOptions().isPacketListEnabled()) {
                            int resp = JOptionPane.showConfirmDialog(null, "", "Is from server ?", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (resp != JOptionPane.CANCEL_OPTION) {
                                FakePacket packet = new FakePacket(Files.readAllBytes(file.toPath()), resp == JOptionPane.YES_OPTION);
                                packet.setName(file.getName());
                                snifferWindow.incomingPacket(packet);
                                select(packet);
                            }
                        } else {
                            if (selectedValue != null) {
                                int resp = JOptionPane.showConfirmDialog(null, "This will overwrite the current content, do you want to proceed ?", "Erase content", JOptionPane.YES_NO_OPTION);
                                if (resp == JOptionPane.YES_OPTION) {
                                    var dataPacket = new FakePacket(Files.readAllBytes(file.toPath()), false);
                                    dataPacket.setName(file.getName());
                                    select(dataPacket);
                                }
                            } else {
                                var dataPacket = new FakePacket(Files.readAllBytes(file.toPath()), false);
                                dataPacket.setName(file.getName());
                                select(dataPacket);
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    log.error("DND error", e);
                }
            }
        });

        view.setSelectionColor(new Color(182, 218, 255));
        view.setFontColorAsciiView(Color.black);

        mainPanel.setLayout(new BorderLayout());
        topNode = new IconNode();
        messageStructure = new JTree(topNode);
        messageStructure.setCellRenderer(new WakfuLibTreeCellRenderer());
        messageStructure.putClientProperty("JTree.lineStyle", "None");
        JSplitPane bottomSpliter = new JSplitPane();
        bottomSpliter.setResizeWeight(1);
        bottomSpliter.setLeftComponent(new JScrollPane(messageStructure));
        messageStructure.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton gotoButton = ExternalPacketViewButtonFactory.gotoButton(view);
        viewListeners.add(new JComponentEnablingViewListener(gotoButton));
        buttonPanel.add(gotoButton);
        JButton offsetButton = ExternalPacketViewButtonFactory.offsetButton(view);
        viewListeners.add(new JComponentEnablingViewListener(offsetButton));
        buttonPanel.add(offsetButton);

        viewFile = new JButton("Open File");
        viewFile.addActionListener(e -> {
            if (selectedValue instanceof FileLinkable) {
                File file = new File(((FileLinkable) selectedValue).getFile());
                if (!file.exists()) {
                    return;
                }
                snifferWindow.loadAction(file);
            }
        });
        viewListeners.add(new ConditionalJComponentEnablingViewListener(viewFile, p -> p instanceof FileLinkable));
        buttonPanel.add(viewFile);

        debug = new JButton("Debug");
        debug.setToolTipText("Open a debugtree for the packet");
        viewListeners.add(new ConditionalJComponentEnablingViewListener(debug, p -> selectedValue.getSerializer() != null));
        debug.addActionListener(e -> {
            if (selectedValue.getSerializer() == null) {
                JOptionPane.showMessageDialog(null, "This packet has no serializer", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Message<?> res;
            try {
                ByteBuffer wrap = ByteBuffer.wrap(selectedValue.getData());
                wrap.getShort();
                if (! selectedValue.isFromServer()) {
                    wrap.get();
                }
                wrap.getShort();
                res = selectedValue.getSerializer().unserialize(wrap);
            } catch (Exception x) {
                x.printStackTrace();
                new ExceptionDialog("Error", x).setVisible(this.view);
                return;
            }
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
            var debuggerTree = new JTree(root);
            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) debuggerTree.getCellRenderer();
            renderer.setLeafIcon(null);
            debuggerTree.addMouseListener(new DebuggerTreePopulator.DebuggerTreeMouseListener(debuggerTree));
            DebuggerTreePopulator.parse(res, root);
            final JFrame parent = new JFrame(selectedValue.getToString());
            parent.setPreferredSize(new Dimension(300, 550));
            parent.add(new JScrollPane(debuggerTree));
            debuggerTree.expandRow(0);
            parent.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            parent.pack();
            parent.setVisible(true);
        });
        buttonPanel.add(debug);
        JButton copyAsHex = new JButton("Copy as hex");
        copyAsHex.addActionListener(a -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(
            OutPacket.simpleBuffer().writeBytes(selectedValue.getData()).getBuffer(true).replaceAll("\\s", "")
        ), null));
        buttonPanel.add(copyAsHex);
        JButton copyMessageClassButton = new JButton("Copy message class name");
        viewListeners.add(new JComponentEnablingViewListener(copyMessageClassButton));
        copyMessageClassButton.setToolTipText("Copy the class name of this message");
        copyMessageClassButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(lastPacketName), null);
            }
        });
        buttonPanel.add(copyMessageClassButton);

        JButton testencodeimpl = new JButton("Serialize");
        debug.setToolTipText("Serialize the packet in a new window");
        viewListeners.add(new ConditionalJComponentEnablingViewListener(testencodeimpl, p -> selectedValue.getSerializer() != null));
        testencodeimpl.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message<?> serializer = selectedValue.getSerializer();
                if (serializer == null) {
                    JOptionPane.showMessageDialog(null, "This packet has no serializer", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ByteBuffer wrap = ByteBuffer.wrap(selectedValue.getData());
                wrap.getShort();
                if (! selectedValue.isFromServer()) {
                    wrap.get();
                }
                wrap.getShort();
                var unserialize = serializer.unserialize(wrap);
                //noinspection deprecation
                unserialize.setOpCode(selectedValue.getSerializer().getOpCode());
                var data = unserialize.encode();
                data.finish();
                var bytes = data.toByteArray();
                JOptionPane.showMessageDialog(null, Arrays.equals(bytes, selectedValue.getData()), "Encode = decode ?", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonPanel.add(testencodeimpl);

        bottomSpliter.setRightComponent(buttonPanel);
        bottomSpliter.setDividerLocation(0.5);
        var hexViewMouseListener = new JTreeHexViewMouseListener(messageStructure, view);
        messageStructure.addTreeSelectionListener(hexViewMouseListener);
        messageStructure.addMouseListener(hexViewMouseListener);
        view.addHoverListener(new TreeHexViewHoverListener(messageStructure, view));
        var topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(view, BorderLayout.CENTER);
        topPanel.add(new HexDataViewer(view), BorderLayout.SOUTH);
        //noinspection SuspiciousNameCombination
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomSpliter);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        splitPane.setResizeWeight(1);

        JCheckBox hideNullCheckBox = new JCheckBox("Hide null", false);
        viewListeners.add(new JComponentEnablingViewListener(hideNullCheckBox));
        buttonPanel.add(hideNullCheckBox);
        hideNullCheckBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DebuggerTreePopulator.HIDE_NULL = hideNullCheckBox.isSelected();
                updateDesc(selectedValue);
            }
        });

        var settings = Settings.getInstance();
        Options options = settings.getOptions();

        var useAutoStructure = new JCheckBox("Use debug reflective structure", false);
        useAutoStructure.setSelected(options.isAUTO_STRUCTURE_PACKET());
        buttonPanel.add(useAutoStructure);
        useAutoStructure.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.getInstance().getOptions().setAUTO_STRUCTURE_PACKET(useAutoStructure.isSelected());
            }
        });

        Settings.getInstance().registerForOptionChange("AUTO_STRUCTURE_PACKET",
            v -> useAutoStructure.setSelected((boolean) v));

        var useAutoExpand = new JCheckBox("Auto expand tree", false);
        useAutoExpand.setSelected(options.isAUTO_EXPAND_TREE());
        buttonPanel.add(useAutoExpand);
        useAutoExpand.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var selected = useAutoExpand.isSelected();
                Settings.getInstance().getOptions().setAUTO_EXPAND_TREE(selected);
                if (selected) {
                    for (int i = 0; i < messageStructure.getRowCount(); i++) {
                        messageStructure.expandRow(i);
                    }
                }
            }
        });

        Settings.getInstance().registerForOptionChange("AUTO_EXPAND_TREE",
            v -> useAutoExpand.setSelected((boolean) v));

        if (options.getSeparatorPacketViewLocation() == -1) {
            setDividerLocation(splitPane, 0.7);
        } else {
            splitPane.setDividerLocation(options.getSeparatorPacketViewLocation());
        }

        settings.registerForOptionChange("AUTO_STRUCTURE_PACKET", v -> {
            if (this.selectedValue != null) {
                updateDesc(this.selectedValue);
            }
        });

        clear();
    }

    public static void setDividerLocation(JSplitPane splitter, final double proportion) {
        if (splitter.isShowing()) {
            if ((splitter.getWidth() > 0) && (splitter.getHeight() > 0)) {
                splitter.setDividerLocation(proportion);
            } else {
                splitter.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent ce) {
                        splitter.removeComponentListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                });
            }
        } else {
            splitter.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && splitter.isShowing()) {
                        splitter.removeHierarchyListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                }
            });
        }
    }

    @Override
    public void addHexHoverListener(IHexViewHoverListener listener) {
        view.addHoverListener(listener);
    }

    @Override
    public void removeHexHoverListener(IHexViewHoverListener listener) {
        view.removeHexHoverListener(listener);
    }

    @Override
    public void select(DataPacket dataPacket) {
        this.selectedValue = dataPacket;
        view.uncolorizeAll(PACKET_DEFINITION_HIGHLIGHT_LEVEL);
        boolean isNotEmpty = selectedValue.getDef() != null;
        viewListeners.forEach(l -> l.onPacketArrival(dataPacket));
        debug.setEnabled(isNotEmpty);
        view.setEditable(dataPacket instanceof FakePacket);

        updateDesc(selectedValue);
        view.setData(new SimpleDataProvider(selectedValue.getData()));
        view.setDefinitionStatus(JHexView.DefinitionStatus.DEFINED);
        view.setEnabled(true);
    }

    private void updateDesc(DataPacket selectedValue) {
        topNode.removeAllChildren();
        AnalyseResult result;
        selectedValue.commonDef(topNode);
        if (selectedValue.getSerializer() != null && Settings.getInstance().getOptions().isAUTO_STRUCTURE_PACKET()) {
            result = analyse(topNode.children());
            if (result.getException() == null) {
                var selectedValueDataWithoutHeader = result.getBuffer();
                try {
                    Object unserialized;
                    try {
                        unserialized = selectedValue.getSerializer().unserialize(selectedValueDataWithoutHeader);
                    } catch (NotImplementedException e) {
                        if (e.getRes() != null) {
                            unserialized = e.getRes();
                        } else {
                            throw e;
                        }
                    }
                    DebuggerTreePopulator.parse(unserialized, topNode);
                    result = new AnalyseResult(null, null, selectedValueDataWithoutHeader);
                    messageStructure.expandRow(0);
                } catch (Exception e) {
                    result = new AnalyseResult(e, null, selectedValueDataWithoutHeader);
                }
            }
        } else {
            oldDesc(selectedValue);
            result = analyse(topNode.children());
        }
        if (result.getException() == null) {
            if (result.getBuffer().remaining() == 0) {
                topNode.setIcon(OK_ICON);
                messageStructure.setBackground(Settings.getInstance().getOptions().getANALYSE_SUCCESS());
            } else {
                topNode.setIcon(WARN_ICON);
                messageStructure.setBackground(Settings.getInstance().getOptions().getANALYSE_HAS_REMAINING());
            }
        } else {
            result.getException().printStackTrace();
            new ExceptionDialog(result.getException()).setVisible(this.view);
            topNode.setIcon(ERR_ICON);
            if (result.getLastNode() != null) {
                result.getLastNode().setOnError(true);
            }
            messageStructure.setBackground(Settings.getInstance().getOptions().getANALYSE_ERROR());
        }
        viewFile.setVisible(selectedValue instanceof FileLinkable && ((FileLinkable) selectedValue).getFile() != null);
        lastPacketName = formalizeName(selectedValue.getDef().getName());
        topNode.setUserObject(selectedValue.getToString());
        ((DefaultTreeModel) messageStructure.getModel()).reload();
        if (Settings.getInstance().getOptions().isAUTO_EXPAND_TREE()) {
            for (int i = 0; i < messageStructure.getRowCount(); i++) {
                messageStructure.expandRow(i);
            }
        }
    }

    private String formalizeName(String name) {
        if (name.length() > 0) {
            int i = name.length();
            if (Character.isDigit(name.charAt(name.length() - 1))) {
                i = name.lastIndexOf('V');
            }
            return name.substring(0, i);
        }
        return name;
    }

    private void oldDesc(DataPacket selectedValue) {
        var defRoot = selectedValue.getDef().defRoot;
        if (defRoot != null) {
            var children = defRoot.children();
            while (children.hasMoreElements()) {
                topNode.add(((PacketDefinitionNode) children.nextElement()).copy());
            }
        }
    }

    @Override
    public void clear() {
        topNode.setIcon(null);
        topNode.removeAllChildren();
        topNode.setUserObject("");
        messageStructure.setBackground(Color.WHITE);
        ((DefaultTreeModel) messageStructure.getModel()).reload();
        viewListeners.forEach(PacketViewListener::onPacketRemoval);
        view.setData(null);
        view.setDefinitionStatus(JHexView.DefinitionStatus.UNDEFINED);
        view.setEnabled(false);
    }

    @Override
    public void onExit() {
        Settings.getInstance().getOptions().setSeparatorPacketViewLocation(((JSplitPane) mainPanel.getComponent(0)).getDividerLocation());
    }

    @Override
    public Component getComponent() {
        return mainPanel;
    }
}
