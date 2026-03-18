package wakfulib.ui.proxy.model;

import java.io.File;
import java.util.LinkedList;
import java.util.stream.Stream;

public class History {

    private final LinkedList<File> files = new LinkedList<>();
    private int maxSize;

    public History(String[] initFiles, int maxSize) {
        this.maxSize = maxSize;
        if (initFiles == null) return;
        for (String initFile : initFiles) {
            File file = new File(initFile);
            if (file.exists()) {
                files.add(file);
            }
        }
    }

    public void addEntry(File file) {
        int i = files.indexOf(file);
        if (i == -1) {
            files.offerFirst(file);
            if (files.size() == maxSize) {
                files.removeLast();
            }
        } else {
            files.remove(i);
            files.offerFirst(file);
        }
    }

    public Stream<File> recentFiles() {
        return files.stream();
    }

    public void updateSize(int newMaxSize) {
        maxSize = newMaxSize;
    }
}
