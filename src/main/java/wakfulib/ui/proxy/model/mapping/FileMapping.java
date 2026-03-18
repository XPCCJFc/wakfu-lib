package wakfulib.ui.proxy.model.mapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.function.BiConsumer;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMapping implements Mapping {
    @Delegate
    private final InMemoryMapping mapping;
    private final File mappingFile;

    public FileMapping(File mappingFile) {
        this.mappingFile = mappingFile;
        mapping = new InMemoryMapping();
        reload();
    }

    @Override
    public void save() {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(mappingFile)))) {
            mapping.stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> out.println(entry.getValue() + " = " + entry.getKey()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void reload() {
        mapping.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFile))){
            String line;
            int lineCounter = 0;
            while ((line = reader.readLine()) != null) {
                lineCounter = lineCounter + 1;
                String[] split = line.split("=");
                String k = split[1].trim();
                if (k.length() > 0) {
                    if (split.length == 2) {
                        int v = Integer.parseInt(split[0].trim());
                        mapping.put(k, v);
                    } else {
                        log.warn("- Incorrect split line : " + lineCounter);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mapping.clear();
        }
    }
}
