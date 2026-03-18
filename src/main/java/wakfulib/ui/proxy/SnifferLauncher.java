package wakfulib.ui.proxy;

import wakfulib.internal.Version;
import wakfulib.ui.proxy.conf.Configuration;
import wakfulib.ui.proxy.conf.IConfiguration;
import wakfulib.utils.SpringUtilities;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SnifferLauncher extends JPanel {

    public static Version DEFAULT_VERSION = Version.v0_315;
    public static IConfiguration DEFAULT_CONF = Configuration.DEV_ARENARETURNS_LOCAL;
    public static List<IConfiguration> DEFAULT_CONFS = Arrays.stream(Configuration.values()).collect(Collectors.toCollection(ArrayList::new));

    private final JCheckBox sslAuth;
    private final JCheckBox sslWorld;

    public Version getVersion() {
        Object selectedItem = versionJComboBox.getSelectedItem();
        if (selectedItem == null) {
            return DEFAULT_VERSION;
        }
        return (Version) selectedItem;
    }

    public int getLocalPortWorld() {
        return Integer.parseInt(localWorldPort.getText());
    }

    public String getRemoteHostAuth() {
        return remoteAuthAddress.getText();
    }

    public int getRemotePortAuth() {
        return Integer.parseInt(remoteAuthPort.getText());
    }

    public int getLocalPortAuth() {
        return Integer.parseInt(localAuthPort.getText());
    }

    public String getRemoteHostWorld() {
        return remoteWorldAddress.getText();
    }

    public int getRemotePortWorld() {
        return Integer.parseInt(remoteWorldPort.getText());
    }

    public boolean getAuthSSL() {
        return sslAuth.isSelected();
    }

    public boolean getWorldSSL() {
        return sslWorld.isSelected();
    }

    public boolean isOnlyWorld() {
        return ! authEnabled.isSelected();
    }

    private final JTextField remoteAuthAddress = new JTextField(20);
    private final JTextField remoteAuthPort = new JTextField(6);
    private final JTextField localAuthPort = new JTextField(6);
    private final JTextField remoteWorldAddress = new JTextField(20);
    private final JTextField remoteWorldPort = new JTextField(6);
    private final JTextField localWorldPort = new JTextField(6);
    private final JCheckBox authEnabled = new JCheckBox("Auth Enabled");
    private final JComboBox<Version> versionJComboBox;

    public SnifferLauncher() {
        super();
        authEnabled.addActionListener(e -> toggleWorldOnly(authEnabled.isSelected()));

        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel("Auth"));
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        add(new JLabel("LocalPort"));
        add(localAuthPort);
        add(new JLabel("<─────────>  RemoteHost"));
        add(remoteAuthAddress);
        add(new JLabel("RemotePort"));
        add(remoteAuthPort);
        sslAuth = new JCheckBox("SSL", true);
        add(sslAuth);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel("World"));
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        add(new JLabel("LocalPort"));
        add(localWorldPort);
        add(new JLabel("<─────────>   RemoteHost"));
        add(remoteWorldAddress);
        add(new JLabel("RemotePort"));
        add(remoteWorldPort);
        sslWorld = new JCheckBox("SSL", true);
        add(sslWorld);

        versionJComboBox = new JComboBox<>(Version.values());
        versionJComboBox.setSelectedItem(DEFAULT_VERSION);
        add(new JLabel("Version :"));
        add(versionJComboBox);
        add(authEnabled);
        for (int i = 0; i < 2; i++) {
            add(new JLabel());
        }
        JComboBox<IConfiguration> configurationJComboBox = new JComboBox<>(DEFAULT_CONFS.toArray(new IConfiguration[0]));
        configurationJComboBox.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, (value instanceof IConfiguration iConfiguration ? iConfiguration.name() : value), index, isSelected, cellHasFocus);
                return this;
            }
        });
        init(DEFAULT_CONF);
        configurationJComboBox.setSelectedItem(DEFAULT_CONF);
        add(new JLabel("Presets :"));
        add(configurationJComboBox);

        configurationJComboBox.addActionListener(a -> {
                Object selectedItem = configurationJComboBox.getSelectedItem();
                if (selectedItem != null) {
                    init((Configuration) selectedItem);
                }
            }
        );

        SpringUtilities.makeCompactGrid(this,
                5, 7, //rows, cols
                6, 6, //initX, initY
                6, 6);

    }

    private void init(IConfiguration conf) {
        localAuthPort.setText(Objects.toString(conf.getLocalAuthPort()));
        localWorldPort.setText(Objects.toString(conf.getLocalWorldPort()));
        remoteAuthAddress.setText(Objects.toString(conf.getRemoteAuthAddress()));
        remoteWorldAddress.setText(Objects.toString(conf.getRemoteWorldAddress()));
        remoteAuthPort.setText(Objects.toString(conf.getRemoteAuthPort()));
        remoteWorldPort.setText(Objects.toString(conf.getRemoteWorldPort()));
        if (conf.getVersion() != null) {
            versionJComboBox.setSelectedItem(conf.getVersion());
        }
        sslWorld.setSelected(conf.isSslWorld());
        sslAuth.setSelected(conf.isSslAuth());

        toggleWorldOnly(! conf.isOnlyWorld());
    }

    private void toggleWorldOnly(boolean notOnlyWorld) {
        sslAuth.setEnabled(notOnlyWorld);
        localAuthPort.setEnabled(notOnlyWorld);
        remoteAuthAddress.setEnabled(notOnlyWorld);
        remoteAuthPort.setEnabled(notOnlyWorld);
        authEnabled.setSelected(notOnlyWorld);
    }

    public IConfiguration selectedOptionAsConfiguration() {
        if (isOnlyWorld()) {
            return IConfiguration.customNoAuth("Custom", getRemoteHostWorld(), getRemotePortWorld(), getLocalPortWorld(), getVersion(), getWorldSSL());
        }
        return IConfiguration.custom("Custom", getRemoteHostAuth(), getRemotePortAuth(), getLocalPortAuth(),
            getRemoteHostWorld(), getRemotePortWorld(), getLocalPortWorld(), getVersion(), getAuthSSL(), getWorldSSL());
    }
}
