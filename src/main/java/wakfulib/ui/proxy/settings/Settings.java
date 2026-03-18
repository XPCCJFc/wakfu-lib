package wakfulib.ui.proxy.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.ui.proxy.listeners.OptionListener;
import wakfulib.ui.utils.ExceptionDialog;

@Slf4j
public class Settings {

    private static Settings INSTANCE;

    private final String propertiesPath = "./propertiesFile.properties";
    private Properties propertiesFile;
    @Getter
    private Options options;
    private final Map<String, List<OptionListener>> optionListeners = new HashMap<>();

    public static Settings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Settings();
            INSTANCE.loadFromFile();
        }
        return INSTANCE;
    }

    private void loadFromFile() {
        propertiesFile = new Properties();
        File file = new File(propertiesPath);
        options = new Options(this :: updateOptionValue);
        registerKeys();
        if (! file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("Error while creating settings file", e);
                new ExceptionDialog("Error while creating settings file", e).setVisible(true);
            }
            saveToFile();
        }
        try (var input = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            propertiesFile.load(input);
            OptionsSerializer.loadFromProperties(propertiesFile, options);
        } catch (IOException e) {
            log.error("Error while loading settings", e);
            new ExceptionDialog("Error while loading settings", e).setVisible(true);
        }
    }

    private void registerKeys() {
        for (Field optionField : Options.class.getDeclaredFields()) {
            if (optionField.getAnnotation(Options.Hidden.class) != null) continue;
            this.optionListeners.put(optionField.getName(), null);
        }
    }

    public void saveToFile() {
        OptionsSerializer.saveToProperties(propertiesFile, options);
        try (FileWriter output = new FileWriter(propertiesPath)){
            propertiesFile.store(output, null);
        } catch (IOException e) {
            log.error("Error while saving settings file", e);
            new ExceptionDialog("Error while saving settings file", e).setVisible(true);
        }
    }

    public void registerForOptionChange(@NonNull String key, @NonNull OptionListener optionListener) {
        if (optionListener == null) throw new IllegalArgumentException("Listener cannot be null !");
        if (! optionListeners.containsKey(key)) {
            throw new IllegalArgumentException("Invalid key " + key);
        }
        List<OptionListener> listeners = this.optionListeners.get(key);
        if (listeners == null) {
            listeners = new ArrayList<>();
            optionListeners.put(key, listeners);
        }
        listeners.add(optionListener);
    }

    public void updateOptionValue(String key, Object value) {
        Optional.ofNullable(optionListeners.get(key)).ifPresent(listeners -> listeners.forEach(l -> l.onOptionChanged(value)));
    }
}
