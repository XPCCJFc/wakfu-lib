package wakfulib.ui.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.internal.versionable.protocol.ToServerMessage;
import wakfulib.logger.IWakfulibLogger;
import wakfulib.logger.impl.SnifferWakfuLogger;
import wakfulib.logic.proxy.WakfuConnectionChannels;
import wakfulib.ui.proxy.io.Format;
import wakfulib.ui.proxy.io.InBatchLatchedParameter;
import wakfulib.ui.proxy.io.InBatchParameter;
import wakfulib.ui.proxy.model.DataPacket;
import wakfulib.ui.proxy.model.FakePacket;
import wakfulib.ui.proxy.model.History;
import wakfulib.ui.proxy.model.mapping.FileMapping;
import wakfulib.ui.proxy.model.Nameable;
import wakfulib.ui.proxy.model.Packet;
import wakfulib.ui.proxy.model.WakfuPacket;
import wakfulib.ui.proxy.model.def.PacketDefinition;
import wakfulib.ui.proxy.model.mapping.InMemoryMapping;
import wakfulib.ui.proxy.model.mapping.Mapping;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.ui.proxy.view.MappingEditor;
import wakfulib.ui.proxy.view.ReplayWindow;
import wakfulib.ui.proxy.view.packetList.FilterPanel;
import wakfulib.ui.proxy.view.packetList.PacketList;
import wakfulib.ui.proxy.view.packetview.HexPacketView;
import wakfulib.ui.proxy.view.packetview.PacketView;
import wakfulib.ui.proxy.view.packetview.impl.ExternalPacketView;
import wakfulib.ui.proxy.view.packetview.impl.PacketCounter;
import wakfulib.ui.proxy.view.scripting.ScriptingPanel;
import wakfulib.ui.utils.ExceptionDialog;
import wakfulib.ui.utils.IconUtils;
import wakfulib.ui.utils.LoadingPopup;
import wakfulib.utils.data.Tuple;

@Slf4j
public class SnifferWindow extends JFrame {

    public static final Options OPTIONS = Settings.getInstance().getOptions();
    public static final String TITLE = "Wakfu Sniffer";
    public static boolean preventExit = true;

    @Setter
    private static Runnable noAuthInitializer;

    private final PacketView packetView;
    @Nullable
    private final PacketList packetListPanel;
    @Nullable
    private final PacketCounter packetCounter;
    @Getter
    private final SnifferMenu menu;
    private final History history;
    @Getter
    private final IWakfulibLogger logManager;
    @Nullable
    private final FilterPanel filterPanel;
    @Nullable
    private final TableRowSorter<TableModel> sorter;
    @Getter
    private final SnifferOptions snifferOptions;
    @Getter
    private Mapping mapping;
    @Setter
    private Runnable onExit = null;
    private JFrame mappingEditor;
    private MappingEditor comp;
    @Getter
    @Nullable
    private JSplitPane backPanel;
    @Setter
    private WakfuConnectionChannels wakfuConnectionChannels;

    public SnifferWindow(Version defaultVersion) {
        this(defaultVersion, new SnifferOptions());
    }

    public SnifferWindow(Version defaultVersion, @NonNull SnifferOptions snifferOptions) {
        super(TITLE);
        mapping = new InMemoryMapping();
        this.snifferOptions = snifferOptions.toBuilder().build();
        if (snifferOptions.isHideAuth()) {
            if (noAuthInitializer == null) {
                log.error("No auth initializer set but hideAuth asked !");
                throw new RuntimeException();
            }
        }
        var appIcon = getAppIcon(snifferOptions.isSniffing());
        if (appIcon != null) {
            setIconImage(appIcon);
        }
        backPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        backPanel.setResizeWeight(0);
        menu = new SnifferMenu(this);

        if (snifferOptions.isSniffing()) { //FIXME plutot permettre des addon de l'exterieur plutot qu'en param
            menu.addReplayMenu();
        }
        final var settings = Settings.getInstance();
        final Options option = settings.getOptions();

        history = new History(option.getRecentsFiles(), option.getHISTORY_SIZE());
        settings.registerForOptionChange("HISTORY_SIZE", v -> history.updateSize((int) v));
        menu.updateRecent(history.recentFiles());
        setJMenuBar(menu);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        packetView = new ExternalPacketView(this);

        if (snifferOptions.isPacketListEnabled()) {
            packetListPanel = new PacketList(this);
            settings.registerForOptionChange("DISPLAY_TIMESTAMP", v -> packetListPanel.revalidate());
            packetListPanel.getSelectionModel().addListSelectionListener(l -> {
                if (!l.getValueIsAdjusting()) {
                    int index = packetListPanel.getSelectionModel().getAnchorSelectionIndex();
                    if (index != -1) {
                        try {
                            Object selected = packetListPanel.getModel().getValueAt(packetListPanel.convertRowIndexToModel(index), 0);
                            if (selected instanceof DataPacket) {
                                packetView.select((DataPacket) selected);
                            }
                        } catch (Exception e) {
                            log.error("Ignore while selection packet", e);
                        }
                    } else {
                        packetView.clear();
                    }
                }
            });

            sorter = new TableRowSorter<>();
            packetListPanel.setRowSorter(sorter);
            sorter.setModel(packetListPanel.getModel());
            filterPanel = new FilterPanel(sorter, packetListPanel);
            JPanel leftComp = new JPanel();
            leftComp.setLayout(new BorderLayout());
            JButton clear = new JButton("Delete all packets");
            clear.setIcon(IconUtils.loadIcon("/icons/trash.png"));
            packetCounter = new PacketCounter();
            clear.addActionListener(a -> {
                packetListPanel.clear();
                packetCounter.reset();
                packetView.clear();
                System.gc();
                menu.hideFileVersionSubMenu();
            });
            leftComp.add(clear, BorderLayout.NORTH);
            JScrollPane packetScrollPane = new JScrollPane(packetListPanel);
            leftComp.add(packetScrollPane, BorderLayout.CENTER);
            JPanel statusPanel = new JPanel();
            statusPanel.setLayout(new BorderLayout());
            statusPanel.add(packetCounter, BorderLayout.CENTER);
            JButton showFilterButton = new JButton("...");
            showFilterButton.setToolTipText("Show more options");
            JButton resetCounterButton = new JButton("R");
            resetCounterButton.setToolTipText("Reset the packet counter");
            resetCounterButton.addActionListener(a -> packetCounter.reset());
            filterPanel.setVisible(false);
            showFilterButton.addActionListener(a -> filterPanel.setVisible(!filterPanel.isVisible()));
            statusPanel.add(filterPanel, BorderLayout.NORTH);
            statusPanel.add(resetCounterButton, BorderLayout.WEST);
            final JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            final JButton toSelected = new JButton();
            toSelected.setEnabled(false);
            toSelected.setIcon(IconUtils.loadIcon("/icons/locate.png"));
            toSelected.setToolTipText("Scroll to selected packet");
            toSelected.addActionListener(a -> packetListPanel.showSelection());
            final JButton addFakePacket = new JButton();
            addFakePacket.setIcon(IconUtils.loadIcon("/icons/plus.png"));
            addFakePacket.setToolTipText("Add a custom/fake packet to the packet list");
            addFakePacket.addActionListener(a -> {
                var minSelectionIndex = packetListPanel.getSelectionModel().getMinSelectionIndex();
                var i = packetListPanel.insertBlankPacket(minSelectionIndex > -1 ?
                    (packetListPanel.convertRowIndexToModel(minSelectionIndex) + 1) : -1, true);
                packetListPanel.setRowSelectionInterval(i, i);
            });
            final JButton deletePacket = new JButton();
            deletePacket.setEnabled(false);
            deletePacket.setIcon(IconUtils.loadIcon("/icons/trash.png"));
            deletePacket.setToolTipText("Delete the selected packet");
            deletePacket.addActionListener(a -> removeSelectedAction(-1));
            packetListPanel.getSelectionModel().addListSelectionListener(e -> {
                if (! e.getValueIsAdjusting()) {
                    var selectionNotEmpty = ! packetListPanel.getSelectionModel().isSelectionEmpty();
                    deletePacket.setEnabled(selectionNotEmpty);
                    toSelected.setEnabled(selectionNotEmpty);
                }
            });
            buttonPanel.add(addFakePacket);
            buttonPanel.add(deletePacket);
            buttonPanel.add(toSelected);
            buttonPanel.add(showFilterButton);
            statusPanel.add(buttonPanel, BorderLayout.EAST);
            leftComp.add(statusPanel, BorderLayout.SOUTH);
            backPanel.setLeftComponent(leftComp);
            backPanel.setRightComponent(packetView.getComponent());
            setContentPane(backPanel);
        } else {
            sorter = null;
            filterPanel = null;
            packetListPanel = null;
            packetCounter = null;

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(packetView.getComponent(), BorderLayout.CENTER);
            setContentPane(contentPane);
        }
        setLocation(option.getX(), option.getY());
        setPreferredSize(new Dimension(option.getW(), option.getH()));
        setSize(new Dimension(option.getW(), option.getH()));
        this.setExtendedState(option.getExtendedState());
        pack();
        logManager = new SnifferWakfuLogger(this);
        backPanel.setDividerLocation(option.getSeparatorPacketListLocation());
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
            if (SnifferWindow.preventExit && snifferOptions.isPacketListEnabled() && packetListPanel.getRowCount() != 0) {
                int selected = JOptionPane.showConfirmDialog(null, "Voulez-vous sauvegarder votre session avant de quitter ?");
                if (selected == JOptionPane.YES_OPTION) {
                    saveAction();
                } else if (selected == JOptionPane.CANCEL_OPTION) {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                    return;
                }
            }
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            final Window window = winEvt.getWindow();
            option.setExtendedState(SnifferWindow.this.getExtendedState());
            option.setX(window.getX());
            option.setY(window.getY());
            option.setW(window.getWidth());
            option.setH(window.getHeight());
            option.setSeparatorPacketListLocation(getBackPanel().getDividerLocation());
            option.setRecentsFiles(history.recentFiles().map(File :: getAbsolutePath).toArray(String[] :: new));
            packetView.onExit();
            settings.saveToFile();
            if (onExit != null) {
                onExit.run();
            }
            }
        });
        setVisible(true);

        messageMapClient = new HashMap<>();
        messageMapServer = new HashMap<>();

        onVersionChange(defaultVersion);
        Version.registerForVersionChanged(this::onVersionChange);
    }

    public static BufferedImage getAppIcon(boolean sniffing) {
        return IconUtils.loadImage("/icons/little_logo" + (sniffing ? "_on" : "") + ".png");
    }

    public void reverseWindows() {
        if (snifferOptions.isPacketListEnabled()) {
            Component leftComponent = backPanel.getLeftComponent();
            Component rightComponent = backPanel.getRightComponent();
            int dividerLocation = backPanel.getDividerLocation();
            var resizeWeight = backPanel.getResizeWeight();
            backPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rightComponent, leftComponent);
            backPanel.setDividerLocation(this.getWidth() - dividerLocation);
            backPanel.setResizeWeight(1 - resizeWeight);
            setContentPane(backPanel);
            this.pack();
            repaint();
        }
    }

    public void loadMappingAction() {
        if (snifferOptions.isPacketListEnabled()) {
            chooseFile(selectedFile -> {
                mapping = new FileMapping(selectedFile);
                final List<WakfuPacket> packets = packetListPanel.clearWithSave();
                reloadMapping();
                packets.forEach(this :: incomingPacket);
                menu.mappingLoaded();
            }, "Choisir un fichier de mapping", "Choisir un fichier de log", "Wakfu Sniffer mapping file", "wfkmap");
        }
    }

    private void reloadMapping() {
        if (snifferOptions.isPacketListEnabled()) {
            packetCounter.reset();
            messageMapClient.clear();
            messageMapServer.clear();
            mapping.reload();
            VersionRegistry.updateMapping(mapping);
            for (Object value : VersionRegistry.registeredClasses()) {
                if (value instanceof ToClientMessage) {
                    ToClientMessage tcMessage = (ToClientMessage) value;
                    messageMapClient.put(tcMessage.getOpCode(), tcMessage);
                }
                if (value instanceof ToServerMessage) {
                    ToServerMessage tcMessage = (ToServerMessage) value;
                    messageMapServer.put(tcMessage.getOpCode(), tcMessage);
                }
            }
            packetListPanel.repaint();
        }
    }

    private final Map<Integer, Message<?>> messageMapClient;
    private final Map<Integer, Message<?>> messageMapServer;

    public void incomingPacket(Packet packet) {
        if (snifferOptions.isPacketListEnabled()) {
            if (packet instanceof DataPacket) {
                mapPacket((DataPacket) packet);
            }
            packetCounter.increment();
            packetListPanel.incomingPacket(packet);
        }
    }

    private void incomingWakfuPacketBatch(List<? extends Packet> packet) {
        for (Packet packet1 : packet) {
            if (packet1 instanceof DataPacket) {
                mapPacket((DataPacket) packet1);
            }
        }
        packetCounter.increment(packet.size());
        packetListPanel.incomingPackets(packet);
    }

    private void mapPacket(DataPacket packet) {
        Message<?> message;
        if (! packet.isFromServer()) {
            message = messageMapServer.get(packet.getOpcode());
        } else {
            message = messageMapClient.get(packet.getOpcode());
        }
        if (message != null) {
            var def = message.def();
            if (def != null) {
                packet.link(message, def);
            } else {
                packet.link(message, PacketDefinition.fromClass(message.getClass()));
            }
        }
        String name = mapping.getByOp(packet.getOpcode());
        if (name != null && packet instanceof Nameable) {
            ((Nameable)packet).setName(name);
        }
    }

    public void loadFromClipboardAction() {
        var contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
        if (! contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            logManager.error("Cannot read string hex dump from keyboard");
            JOptionPane.showMessageDialog(this, "Cannot read string hex dump from keyboard", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            var transferData = (String) contents.getTransferData(DataFlavor.stringFlavor);

            if (snifferOptions.isPacketListEnabled()) {
                packetCounter.reset();
                packetListPanel.clear();
                Format.HEXDUMP.readInternal(new ByteArrayInputStream(transferData.getBytes()), null).forEach(this :: incomingPacket);
            } else {
                Format.HEXDUMP.readInternal(new ByteArrayInputStream(transferData.getBytes()), null)
                    .stream()
                    .findAny()
                    .ifPresentOrElse(packetView::select, () -> {
                        logManager.error("No packet found in clipboard");
                        JOptionPane.showMessageDialog(this, "No packet found in clipboard", "Error", JOptionPane.ERROR_MESSAGE);
                    });
            }
            setTitle("Clipboard");
        } catch (Exception e) {
            logManager.error("Error while reading hex dump from keyboard", e);
            new ExceptionDialog("Error while reading hex dump from keyboard", e.getLocalizedMessage(), e).setVisible(SnifferWindow.this);
            return;
        }

    }

    public void loadAction() {
        if (snifferOptions.isPacketListEnabled()) {
            chooseFile(this::loadAction, "Choisir un fichier de log", "Wakfu Sniffer dump file", Format.READABLE_EXTENSIONS);
        }
    }

    public void loadAction(File selectedFile) {
        if (snifferOptions.isPacketListEnabled()) {
            packetCounter.reset();
            packetListPanel.clear();
            readBatch(new InBatchParameter<>(selectedFile) {
                @Override
                public void process(List<? extends DataPacket> packetList) {
                    incomingWakfuPacketBatch(packetList);
                }

                @Override
                public void done() {
                    setTitle(selectedFile.getName());
                    Version flagVersion = Format.ReadersFlags.getVersion();
                    if (flagVersion != Version.UNKNOWN) {
                        menu.showFileVersionSubMenu(flagVersion);
                        if (flagVersion.compareTo(Version.getCurrent()) != 0) {
                            int res = JOptionPane.showConfirmDialog(SnifferWindow.this,
                                "Le fichier lu viens de la version " + flagVersion + " voulez-vous changer de version ?",
                                "Ce packet viens d'une autre version", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE
                            );
                            if (res == JOptionPane.YES_OPTION) {
                                Version.setCurrent(flagVersion);
                            }
                        }
                    } else {
                        menu.hideFileVersionSubMenu();
                    }
                    history.addEntry(selectedFile);
                    menu.updateRecent(history.recentFiles());
                }
            }).execute();
        }
    }

    private SwingWorker<Integer, List<? extends DataPacket>> readBatch(InBatchParameter<List<? extends DataPacket>> parameter) {
        return new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                publish(Format.readAll(parameter.getFile()));
                return 0;
            }

            @Override
            protected void process(List<List<? extends DataPacket>> chunks) {
                for (var chunk : chunks) {
                    parameter.process(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    parameter.done();
                } catch (Exception e) {
                    logManager.error("Error while executing batch operation", e);
                    new ExceptionDialog(e.getLocalizedMessage(), e).setVisible(SnifferWindow.this);
                }
            }
        };
    }

    public void saveAction() {
        if (packetListPanel == null) {
            var packet = packetView.getSelectedValue();
            if (packet == null) {
                JOptionPane.showMessageDialog(this, "Nothing to save !", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (! (packet instanceof FakePacket fakePacket)) {
                JOptionPane.showMessageDialog(this, "Can't save " + packet.getClass().getSimpleName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fakePacket.getToString())))) {
                out.write(fakePacket.getData());
            } catch (Exception e) {
                logManager.error("!", e);
            }
            return;
        }
        if (packetListPanel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "You have no packet to save !", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        chooseFileToSave(selectedFile -> packetListPanel.serialize(selectedFile, packetListPanel.modelIterator()),
            "Selectionner le dosier de destination des logs", "Wakfu Sniffer dump file", Format.SNOUFLE_DATA.getExtension(), Format.HEXDUMP);
    }

    public void generateMappingAction() {
        if (packetListPanel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "You have no packet loaded !", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        chooseFileToSave(packetListPanel :: generateMapping,
            "Selectionner le dossier de destination du mapping", "Wakfu Sniffer mapping file", "wfkmap");
    }

    public void chooseFileToSave(Consumer<File> fileConsumer, String title, String description, String extensions, Format... saveFormat) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
        for (Format format : saveFormat) {
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(format.name(), format.getExtension()));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            boolean typeRecognized = false;
            if (chooser.getFileFilter() instanceof FileNameExtensionFilter fileFilter) {
                int i = selectedFile.getAbsolutePath().lastIndexOf('.');
                if (i != -1) {
                    String ex = selectedFile.getAbsolutePath().substring(i);
                    for (String extension : fileFilter.getExtensions()) {
                        if (ex.equals('.' + extension)) {
                            typeRecognized = true;
                            break;
                        }
                    }
                }
                if (!typeRecognized) {
                    selectedFile = new File(selectedFile + "." + fileFilter.getExtensions()[0]);
                    typeRecognized = true;
                }
            }
            if (!typeRecognized && ! selectedFile.getAbsolutePath().endsWith("." + extensions)){
                selectedFile = new File(selectedFile + "." + extensions);
            }
            if (selectedFile.exists()) {
                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    "The file " + selectedFile.getName() + " already exists, do you want to override it ?",
                    "Continue ?", JOptionPane.YES_NO_OPTION)) {
                    return;
                }
            } else {
                try {
                    selectedFile.createNewFile();
                } catch (IOException e) {
                    logManager.error("Error while saving packet", e);
                    return;
                }
            }
            fileConsumer.accept(selectedFile);
        }
    }

    public void chooseFile(Consumer<File> fileConsumer, String title, String description, String... extensions) {
//        if (OPTIONS.nativeFileDialog) {
//            PlatformImpl.startup(() -> {
//                FileChooser d = new FileChooser();
//                d.setTitle(title);
//                d.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(description, extensions));
//                File selectedFile = d.showOpenDialog(null);
//                if (selectedFile == null) {
//                    return;
//                }
//                if (! selectedFile.exists()) {
//                    JOptionPane.showMessageDialog(this, "This file doesn't exist");
//                } else {
//                    fileConsumer.accept(selectedFile);
//                }
//            });
//        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogTitle(title);
            chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (! selectedFile.exists()) {
                    JOptionPane.showMessageDialog(this, "This file doesn't exist");
                } else {
                    fileConsumer.accept(selectedFile);
                }
            }
//        }
    }

    public void editMappingAction() {
        if (mappingEditor == null) {
            mappingEditor = new JFrame();
            mappingEditor.setIconImage(getIconImage());
            mappingEditor.setLayout(new BorderLayout());
            comp = new MappingEditor(mapping);
            mappingEditor.add(comp, BorderLayout.CENTER);
            mappingEditor.setTitle("Mapping Editor");
            mappingEditor.setSize(100, 100);
            mappingEditor.setDefaultCloseOperation(HIDE_ON_CLOSE);
            mappingEditor.pack();
        }
        mappingEditor.setVisible(true);
    }

    public void saveMappingAction() {
        mapping.save();
    }

    public void modifyMappingValue(int opCode) {
        editMappingAction();
        comp.editOpCode(opCode);
    }

    private void onVersionChange(Version version) {
        if (Version.getCurrent() != version) {
            Version.setCurrent(version);
        }
        reloadForCurrentVersion();
    }

    public void reloadForCurrentVersion() {
        reloadMapping();
        packetListPanel.modelIterator().forEachRemaining(p -> {
            if (p instanceof DataPacket) {
                this.mapPacket((DataPacket) p);
            }
        });
    }

    public void openScriptWindow() {
        ScriptingPanel scriptingPanel = new ScriptingPanel();
        if (packetView instanceof HexPacketView) {
            scriptingPanel.setPacketView(((HexPacketView) packetView));
        }
        scriptingPanel.setLocationRelativeTo(this);
        scriptingPanel.setVisible(true);
    }

    public void enrichMapping() {
        for (Object value : VersionRegistry.registeredClasses()) {
            if (value instanceof Message tcMessage) {
                if (tcMessage.getOpCode() == -1) continue;
                String simpleName = tcMessage.getClass().getSuperclass().getSimpleName();
                Integer op = mapping.getByName(simpleName);
                if (op == null) {
                    mapping.put(simpleName, tcMessage.getOpCode());
                } else if (op != tcMessage.getOpCode()) {
                    mapping.put(simpleName + "$", tcMessage.getOpCode());
                }
            }
        }
        this.mapping.save();
    }

    private void walkPacketsFileInDirectory(Consumer<File> directoryChoosed, Consumer<DataPacket> wakfuPacketConsumer, Runnable doneListener) {
        walkPacketsFileInDirectoryBatched(directoryChoosed, batch -> batch.forEach(wakfuPacketConsumer), doneListener);
    }

    private void walkPacketsFileInDirectoryBatched(Consumer<File> directoryChoosed, Consumer<List<? extends DataPacket>> wakfuPacketConsumer, Runnable doneListener) {
        chooseDirectory(dir -> {
            directoryChoosed.accept(dir);
            AtomicLong packetCounter = new AtomicLong(0);
            try (Stream<Path> walk = Files.walk(dir.toPath(), 1)) {
                setTitle("Scanning directory : " + dir.getAbsolutePath() + "...");
                var workers = walk.filter(path -> {
                        final String pathAsString = path.toString();
                        return Format.getByName(pathAsString) != null;
                    }).map(f1 -> new InBatchLatchedParameter<List<? extends DataPacket>>(f1.toFile()) {
                            @Override
                            public void process(List<? extends DataPacket> packetList) {
                                packetCounter.getAndAdd(packetList.size());
                                wakfuPacketConsumer.accept(packetList);
                            }

                            @Override
                            public void done() {
                                super.done();
                                setTitle(
                                    "Result of directory scanning : " + dir.getAbsolutePath() + " (" + packetCounter.get() + " packets analysed)");
                            }
                        }
                    ).map(parameter -> new Tuple<>(readBatch(parameter), parameter))
                    .toList();
                CountDownLatch count = new CountDownLatch(workers.size());
                workers.forEach(t -> t._2.setCountDownLatch(count));
                workers.forEach(t -> t._1.execute());
                new Thread(() -> {
                    try {
                        count.await();
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> new ExceptionDialog(e).setVisible(true));
                    } finally {
                        doneListener.run();
                    }
                }).start();
            } catch (Exception e) {
                logManager.error("Error while executing batching operation", e);
            }
        }, "Choose a directory to scan");
    }

    public void search() {
        Integer opcode = null;
        String s = JOptionPane.showInputDialog(this, "What opcode are you looking for ?");
        if (s != null) {
            try {
                opcode = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid opcode !");
            }
        }
        if (opcode == null) {//cancelled
            return;
        }
        Integer finalOpcode = opcode;
        packetListPanel.clear();
        LoadingPopup loadingPopup = new LoadingPopup(this, "Search in progress");
        walkPacketsFileInDirectory(d -> loadingPopup.setVisible(true),
            w -> {
                if (w.getOpcode() == finalOpcode) {
                    incomingPacket(w);
                }
            }, loadingPopup :: dispose
        );
    }

    public void collectUnknownPackets() {
        if (snifferOptions.isPacketListEnabled()) {
            if (mapping instanceof InMemoryMapping) {
                final int res = JOptionPane.showConfirmDialog(this, "Aucun mapping n'est chargé êtes-vous sur de vouloir continuer ?");
                if (res != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            Set<Integer> packetOpSet = new HashSet<>();
            LoadingPopup loadingPopup = new LoadingPopup(this, "Search in progress");
            walkPacketsFileInDirectory(d -> {
            }, w -> {
                if (packetOpSet.contains(w.getOpcode())) return;
                boolean discardPacket;
                if (!w.isFromServer()) {
                    discardPacket = messageMapServer.containsKey(w.getOpcode());
                } else {
                    discardPacket = messageMapClient.containsKey(w.getOpcode());
                }
                if (!discardPacket) {
                    discardPacket = mapping.containsOp(w.getOpcode());
                }
                if (!discardPacket) {
                    packetOpSet.add(w.getOpcode());
                    incomingPacket(w);
                }
            }, loadingPopup::dispose);
        }
    }

    private void chooseDirectory(Consumer<File> fileConsumer, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (! selectedFile.exists() || ! selectedFile.isDirectory()) {
                JOptionPane.showMessageDialog(this, "This directory doesn't exist or is not valid");
            } else {
                fileConsumer.accept(selectedFile);
            }
        }
    }

    public void addToHideFilter(int i) {
        if (snifferOptions.isPacketListEnabled()) {
            filterPanel.hidePacketOfType(i);
            sorter.allRowsChanged();
        }
    }

    public void removeToHideFilter(int i) {
        if (snifferOptions.isPacketListEnabled()) {
            filterPanel.hidePacketOfType(i);
            sorter.allRowsChanged();
        }
    }

    public void beginSelect(int rowIndex) {
        if (snifferOptions.isPacketListEnabled()) {
            packetListPanel.beginSelect(rowIndex);
            sorter.allRowsChanged();//TODO optimize
        }
    }

    public void endSelect(int rowIndex) {
        if (snifferOptions.isPacketListEnabled()) {
            packetListPanel.endSelect(rowIndex);
            sorter.allRowsChanged();//TODO optimize
        }
    }

    public void exportSelectedAction() {
        if (snifferOptions.isPacketListEnabled()) {
            var packetIterator = packetListPanel.selectionIterator();
            if (! packetIterator.hasNext()) {
                JOptionPane.showMessageDialog(this, "You have no packet to save !", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            chooseFileToSave(selectedFile -> packetListPanel.serialize(selectedFile, packetIterator),
                "Selectionner le dosier de destination des logs", "Wakfu Sniffer dump file", Format.SNOUFLE_DATA.getExtension(), Format.HEXDUMP);
        }
    }

    public void removeSelectedAction(int orElseIndex) {
        if (snifferOptions.isPacketListEnabled()) {
            packetListPanel.removeSelected(orElseIndex);
        }
    }

    public void openReplayWindow() {
        int selectedRow = packetListPanel.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "You have no packet selected !", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Packet selected = (Packet) packetListPanel.getModel().getValueAt(packetListPanel.convertRowIndexToModel(selectedRow), 0);
        if (! (selected instanceof DataPacket)) {
            JOptionPane.showMessageDialog(this, "Cannot replay a non data packet (selected is a " + selected.getClass().getSimpleName() + ")", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new ReplayWindow((DataPacket) selected, this).setVisible(true);
    }

    public void countPacket() {
        if (snifferOptions.isPacketListEnabled()) {
            LoadingPopup loadingPopup = new LoadingPopup(this, "Search in progress");
            AtomicInteger integer = new AtomicInteger(0);
            walkPacketsFileInDirectory(d -> loadingPopup.setVisible(true),
                w -> integer.incrementAndGet(),
                () -> {
                    loadingPopup.dispose();
                    JOptionPane.showMessageDialog(this, integer.get() + " packets found !");
                }
            );
        }
    }

    public void send(ByteBuf data, boolean toClient) {
        if (wakfuConnectionChannels == null) {
            JOptionPane.showMessageDialog(this, "Not in proxy mode", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Channel backProvider = toClient ? wakfuConnectionChannels.getBackProvider() : wakfuConnectionChannels.getFrontProvider();
        backProvider.writeAndFlush(data);
    }

    public void changePacketName(Packet p) {
        if (p instanceof WakfuPacket) {
            modifyMappingValue(((WakfuPacket) p).getOpcode());
        } else {
            String name = JOptionPane.showInputDialog("New name ?");
            if (name != null && name.trim().length() > 0) {
                ((Nameable) p).setName(name.trim());
            }
        }
    }
}
