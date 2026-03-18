package wakfulib.ui.proxy;

import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import wakfulib.internal.Version;
import wakfulib.ui.proxy.settings.Options;
import wakfulib.ui.proxy.settings.Settings;
import wakfulib.ui.utils.CustomDialog;
import wakfulib.ui.utils.ExceptionDialog;
import wakfulib.ui.utils.IconUtils;
import wakfulib.ui.utils.JMenuItemBuilder;
import wakfulib.ui.utils.JScrollPopupMenu;
import wakfulib.ui.utils.RadioButtonGroupEnumAdapter;

@Slf4j
public class SnifferMenu extends JMenuBar {
    private final SnifferWindow snifferWindow;
    private JMenuItem editMappingMenuItem;
    private JMenuItem saveMappingMenuItem;
    private JMenuItem reloadMappingMenuItem;
    private JMenuItem enrichMappingMenuItem;
    private JMenu recentFiles;
    private JMenu version;

    public SnifferMenu(SnifferWindow snifferWindow) {
        this.snifferWindow = snifferWindow;
        generateOptionMenu();
    }

    private void generateOptionMenu() {
        JMenu optionsMenu = new JMenu("Options");
        SnifferOptions snifferOptions = snifferWindow.getSnifferOptions();
        try {
            Options options = Settings.getInstance().getOptions();
            final CustomDialog dialog = new CustomDialog(null);
            for (Field optionField : Options.class.getDeclaredFields()) {
                if (optionField.getAnnotation(Options.Hidden.class) != null) continue;
                if (Modifier.isFinal(optionField.getModifiers())) continue;
                optionField.setAccessible(true);
                Class<?> type = optionField.getType();
                String smallOptionName = optionField.getName().toLowerCase().replaceAll("_", " ");
                String optName = smallOptionName.substring(0, 1).toUpperCase() + smallOptionName.substring(1);

                final JMenuItem optionMenuItem;

                if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    //TODO proper localization
                    optionMenuItem = new JCheckBoxMenuItem(optName, (Boolean) optionField.get(options));
                    Settings.getInstance().registerForOptionChange(optionField.getName(), v -> ((JCheckBoxMenuItem) optionMenuItem).setState((boolean) v));
                    optionMenuItem.addActionListener(a -> {
                        try {
                            optionField.set(options, ((JCheckBoxMenuItem) optionMenuItem).getState());
                            Settings.getInstance().updateOptionValue(optionField.getName(), ((JCheckBoxMenuItem) optionMenuItem).getState());
                            } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    optionMenuItem = new JMenuItem(optName + " - " + optionField.get(options));

                    Settings.getInstance().registerForOptionChange(optionField.getName(), v -> {
                        String oldLabel_ = optionMenuItem.getText();
                        optionMenuItem.setText(oldLabel_.substring(0, oldLabel_.indexOf('-') + 2) + v);
                    });
                    optionMenuItem.addActionListener(a -> {
                        dialog.setValidator(CustomDialog.INT_VALIDATOR);
                        try {
                            dialog.setOption(Objects.toString(optionField.get(options)), optName);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        dialog.setVisible(true);
                        String validatedText = dialog.getValidatedText();
                        if (validatedText != null) {
                            int value = Integer.parseInt(validatedText);
                            try {
                                optionField.set(options, value);
                                String oldLabel = optionMenuItem.getText();
                                optionMenuItem.setText(oldLabel.substring(0, oldLabel.indexOf('-') + 2) + Objects.toString(value));
                                Settings.getInstance().updateOptionValue(optionField.getName(), value);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (type.equals(Color.class)) {
                    optionMenuItem = new JMenuItem(optName);
                    optionMenuItem.setBackground((Color) optionField.get(options));
                    Settings.getInstance().registerForOptionChange(optionField.getName(), v -> {
                        optionMenuItem.setBackground((Color) v);
                        optionMenuItem.repaint();
                    });
                    optionMenuItem.addActionListener(a -> {
                        try {
                            Color oldColor = (Color) optionField.get(options);
                            Color value = JColorChooser.showDialog(null, "Choose a color for option " + optName, oldColor);
                            if (value != null) {
                                try {
                                    optionField.set(options, value);
                                    optionMenuItem.setBackground(value);
                                    optionMenuItem.repaint();
                                    Settings.getInstance().updateOptionValue(optionField.getName(), value);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (type.equals(File.class)) {
                    optionMenuItem = new JMenuItem(optName);
                    optionMenuItem.setIcon(IconUtils.loadIcon("/icons/menu-open_dark.png"));
                    optionMenuItem.addActionListener(l -> {
                        try {
                            JFileChooser jFileChooser = new JFileChooser((File) optionField.get(options));
                            Options.FileOptions annotation = optionField.getAnnotation(Options.FileOptions.class);
                            if (annotation != null) {
                                jFileChooser.setFileSelectionMode(annotation.mode());
                            }
                            jFileChooser.setDialogTitle("Choose a value for option " + optName);
                            int res = jFileChooser.showOpenDialog(null);
                            if (res == JFileChooser.APPROVE_OPTION) {
                                optionField.set(options, jFileChooser.getSelectedFile());
                                Settings.getInstance().updateOptionValue(optionField.getName(), jFileChooser.getSelectedFile());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    optionMenuItem = null;
                }

                if (optionMenuItem != null) {
                    if (optionField.getAnnotation(Options.ReadOnly.class) != null) {
                        optionMenuItem.setEnabled(false);
                    }
                    optionsMenu.add(optionMenuItem);
                } else {
                    log.warn("Cannot create a menu item for option '" + optName + "' with type '" + optionField.getType().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.error("Error while generating option menu", e);
            new ExceptionDialog("Error while generating option menu", e).setVisible(this);
        }
        JMenu file = new JMenu("File");
        file.add(new JMenuItemBuilder("Save", e -> snifferWindow.saveAction())
            .withKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
            .withIcon("/icons/menu-saveall_dark.png")
            .build());
        file.add(new JMenuItemBuilder("Open", e -> snifferWindow.loadAction())
            .withKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
            .withIcon("/icons/menu-open_dark.png")
            .build());
        
        file.add(new JMenuItemBuilder("Import from clipboard", e -> snifferWindow.loadFromClipboardAction())
                     .withIcon("/icons/menu-paste_dark.png")
                     .build());
        
        if (snifferOptions.isPacketListEnabled()) {
            file.add(new JMenuItemBuilder("Export Selected", e -> snifferWindow.exportSelectedAction())
                .withKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                .build());
        }
        recentFiles = new JMenu("Open recent");
        file.add(recentFiles);
        
        add(file);
        add(optionsMenu);

        var style = new JMenu("WindowStyle");

        var scrollPopupMenu = new JScrollPopupMenu();
        scrollPopupMenu.setMaximumVisibleRows(25);
        scrollPopupMenu.installOn(style);

        add(style);
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                final List<String> lofsNames = new Reflections("com.formdev.flatlaf").getSubTypesOf(FlatLaf.class).stream().map(Class :: getName)
                    .filter(name -> ! name.contains("$"))
                    .collect(Collectors.toList());
                lofsNames.addAll(Arrays.stream(UIManager.getInstalledLookAndFeels()).map(UIManager.LookAndFeelInfo :: getClassName)
                    .collect(Collectors.toList()));
                return lofsNames;
            }

            @Override
            protected void done() {
                List<String> lofsNames;
                try {
                    lofsNames = get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return;
                }
                ButtonGroup buttonGroup = new ButtonGroup();
                var persistantDefaultLOF = Settings.getInstance().getOptions().getDefaultLOF();
                String defaultLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
                if (persistantDefaultLOF == null) {
                    setLookAndFeel(defaultLookAndFeel);
                } else {
                    try {
                        UIManager.setLookAndFeel(persistantDefaultLOF);
                        defaultLookAndFeel = persistantDefaultLOF;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setLookAndFeel(defaultLookAndFeel);
                }
                for (String lofsName : lofsNames) {
                    final int lastPoint = lofsName.lastIndexOf('.');
                    String label;
                    if (lastPoint != -1) {
                        label = lofsName.substring(lastPoint + 1);
                    } else {
                        label = lofsName;
                    }
                    JMenuItem currentMenu = style.add(new JRadioButtonMenuItem(new AbstractAction(label) {
                        @Serial
                        private static final long serialVersionUID = 7588919504149148501L;

                        public void actionPerformed(ActionEvent e) {
                            setLookAndFeel(lofsName);
                        }
                    }));
                    buttonGroup.add(currentMenu);
                    if (defaultLookAndFeel.equals(lofsName)) {
                        currentMenu.setSelected(true);
                    }
                }
                super.done();
            }
        }.execute();

        version = new JMenu("Version");
        ButtonGroup versionGroup = new ButtonGroup();

        var lockVersionByKey = Settings.getInstance().getOptions().isLOCK_VERSION_BY_KEY();

        RadioButtonGroupEnumAdapter<Version> versionModel = new RadioButtonGroupEnumAdapter<>(Version.class);

        Version.allVersionByKey().forEach((key, value1) -> {
            var fakeSeparator = new JPanel();
            fakeSeparator.setSize(new Dimension(10000, 1));
            var titledBorder = new TitledBorder(new MatteBorder(1, 0, -15, 0, Color.GRAY), key);
            titledBorder.setTitleColor(Color.GRAY);
            fakeSeparator.setBorder(titledBorder);
            version.add(fakeSeparator);
            for (Version value : value1) {
                if (lockVersionByKey && !Version.getCurrent().hasSameKey(value))
                    continue;
                JRadioButtonMenuItem versionCheck = new JRadioButtonMenuItem(value.toString());
                if (value.equals(Version.getCurrent())) {
                    versionCheck.setSelected(true);
                }
                versionCheck.addActionListener(x -> Version.setCurrent(value));
                version.add(versionCheck);
                versionGroup.add(versionCheck);
                versionModel.associate(value, versionCheck);
            }
        });

        Version.registerForVersionChanged(versionModel :: setValue);
        add(version);

        final JMenu mapping = new JMenu("Mapping");

        final JMenuItem loadMapping = new JMenuItem("Load");
        loadMapping.addActionListener(a -> snifferWindow.loadMappingAction());
        mapping.add(loadMapping);

        reloadMappingMenuItem = new JMenuItem("Reload");
        reloadMappingMenuItem.setEnabled(false);
        reloadMappingMenuItem.addActionListener(l -> snifferWindow.reloadForCurrentVersion());
        mapping.add(reloadMappingMenuItem);

        if (snifferOptions.isPacketListEnabled()) {
            final JMenuItem generateMapping = new JMenuItem("Generate");
            generateMapping.addActionListener(a -> snifferWindow.generateMappingAction());
            mapping.add(generateMapping);
        }

        editMappingMenuItem = new JMenuItem("Edit");
        editMappingMenuItem.setEnabled(false);
        editMappingMenuItem.addActionListener(a -> snifferWindow.editMappingAction());
        mapping.add(editMappingMenuItem);

        saveMappingMenuItem = new JMenuItem("Save");
        saveMappingMenuItem.setEnabled(false);
        saveMappingMenuItem.addActionListener(a -> snifferWindow.saveMappingAction());
        mapping.add(saveMappingMenuItem);

        if (snifferOptions.isPacketListEnabled()) {
            enrichMappingMenuItem = new JMenuItem("Enrich");
            enrichMappingMenuItem.setEnabled(false);
            enrichMappingMenuItem.addActionListener(a -> snifferWindow.enrichMapping());
            mapping.add(enrichMappingMenuItem);
        }

        add(mapping);

        if (snifferOptions.isPacketListEnabled()) {
            JMenu reverse = new JMenu("Reverse");
            reverse.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    snifferWindow.reverseWindows();
                    reverse.setSelected(false);
                }
            });
            add(reverse);
        }

        JMenu script = new JMenu("Script");
        script.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                snifferWindow.openScriptWindow();
                script.setSelected(false);
            }
        });
        add(script);

        if (snifferOptions.isPacketListEnabled()) {
            JMenu batchOps = new JMenu("Batch Operations");
            batchOps.add(new JMenuItemBuilder("Collect Unknown",
                e -> snifferWindow.collectUnknownPackets()).build());
            batchOps.add(new JMenuItemBuilder("Search Opcode",
                e -> snifferWindow.search()).build());
            batchOps.add(new JMenuItemBuilder("Count Packet",
                e -> snifferWindow.countPacket()).build());
            add(batchOps);
        }
    }

    public void addReplayMenu() {
        JMenu replay = new JMenu("Replay");
        replay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                snifferWindow.openReplayWindow();
                replay.setSelected(false);
            }
        });
        add(replay);
    }

    private void setLookAndFeel(String clazz) {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
            try {
                Settings.getInstance().getOptions().setDefaultLOF(clazz);
                UIManager.setLookAndFeel(clazz);
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (Exception e) {
                log.error("Error while setting LOF to {}", clazz, e);
                new ExceptionDialog("Error while setting LOF", e).setVisible(true);
            }
        }
    }

    public void mappingLoaded() {
        editMappingMenuItem.setEnabled(true);
        saveMappingMenuItem.setEnabled(true);
        reloadMappingMenuItem.setEnabled(true);
        enrichMappingMenuItem.setEnabled(true);
    }

    public void updateRecent(Stream<File> files) {
        recentFiles.removeAll();
        files.forEach(f -> {
            String name = f.getName();
            int dotIndex = name.indexOf('.');
            if (dotIndex != -1) {
                name = name.substring(0, dotIndex);
            }
            recentFiles.add(new JMenuItemBuilder(name, e -> snifferWindow.loadAction(f)).build());
        });
        if (recentFiles.getItemCount() == 0) {
            JMenuItem noRecentFiles = new JMenuItem("No recent files");
            noRecentFiles.setEnabled(false);
            recentFiles.add(noRecentFiles);
        }
    }

    private boolean lastVersionSubMenuShown = false;

    public void showFileVersionSubMenu(Version version) {
        if (lastVersionSubMenuShown) {
            hideFileVersionSubMenu();
        }
        this.version.add(new JSeparator());
        JMenuItem info = new JMenuItem("Last file version");
        info.setEnabled(false);
        this.version.add(info);

        JMenuItem item = new JMenuItem(version.toString());
        item.addActionListener(l -> Version.setCurrent(version));

        this.version.add(item);
        lastVersionSubMenuShown = true;
    }

    public void hideFileVersionSubMenu() {
        if (lastVersionSubMenuShown) {
            for (int i = 0; i < 3; i++) {
                this.version.remove(this.version.getItemCount() - 1);
            }
        }
        lastVersionSubMenuShown = false;
    }
}
