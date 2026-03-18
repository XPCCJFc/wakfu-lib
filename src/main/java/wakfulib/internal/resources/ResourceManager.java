package wakfulib.internal.resources;

import java.io.File;
import lombok.Getter;
import wakfulib.annotation.NonNull;

/**
 * Manages the resources folder for the library.
 */
public class ResourceManager {

    /**
     * The resources folder.
     */
    @Getter
    private static File RESOURCES_FOLDER;

    /**
     * The absolute path to the resources folder.
     */
    @Getter
    private static String RESOURCES_FOLDER_PATH;

    static {
        setResourcesFolder(new File(""));
    }

    /**
     * Sets the resources folder and updates the resources folder path.
     *
     * @param file the new resources folder
     */
    public static void setResourcesFolder(@NonNull File file) {
        RESOURCES_FOLDER = file;
        RESOURCES_FOLDER_PATH = (file.getAbsolutePath() + "\\").replaceAll("\\\\", "/");
    }

}
