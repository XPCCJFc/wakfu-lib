package wakfulib.ui.proxy.settings;

import java.awt.Color;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;
import javax.swing.JFileChooser;
import lombok.Getter;
import lombok.Setter;
import org.intellij.lang.annotations.MagicConstant;

@Getter
public final class Options {
    boolean AUTO_STRUCTURE_PACKET = true;
    int HISTORY_SIZE = 10;
    boolean DISPLAY_TIMESTAMP = false;
    boolean ALWAYS_BOTTOM_SCROLL = false;
    boolean SAVE_LAST_SCRIPT = true;
    Color SERVER_BACKGROUND = Color.PINK;
    Color CLIENT_BACKGROUND = Color.CYAN;
    Color SSL_BACKGROUND = Color.ORANGE;
    Color CUSTOM_BACKGROUND = Color.LIGHT_GRAY;
    Color FAKE_FOREGROUND = Color.RED;
    boolean REMEMBER_AUTH_PRIVACY_OPTION = false;
    Color ANALYSE_SUCCESS = new Color(119, 255, 119, 255);
    Color ANALYSE_ERROR = Color.ORANGE;
    Color ANALYSE_HAS_REMAINING = Color.RED;
    boolean AUTO_EXPAND_TREE = false;
    boolean LOCK_VERSION_BY_KEY = true;

    @FileOptions(mode = JFileChooser.DIRECTORIES_ONLY)
    File SCRIPTS_DIRECTORY = null;

    @ReadOnly boolean HIDE_AUTH = true;

    @Hidden int x = 0;
    @Setter
    @Hidden String lastScript;
    @Setter
    @Hidden String lastScriptEndianess;
    @Hidden int y = 0;
    @Hidden int w = 500;
    @Hidden int h = 500;
    @Hidden int extendedState = 0;
    @Hidden int separatorPacketListLocation = 300;
    @Hidden int separatorPacketViewLocation = -1;
    @Hidden String[] recentsFiles;

    @Setter
    @Hidden String defaultLOF;

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Hidden {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReadOnly {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FileOptions {
        @MagicConstant(intValues = {JFileChooser.FILES_AND_DIRECTORIES, JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY})
        int mode() default JFileChooser.FILES_AND_DIRECTORIES;
    }


    private final BiConsumer<String, Object> valueChanged;

    Options(BiConsumer<String, Object> valueChanged) {
        this.valueChanged = valueChanged;
        valueChanged.accept("valueChanged", valueChanged);
    }

    public void setAUTO_STRUCTURE_PACKET(boolean AUTO_STRUCTURE_PACKET) {
        this.AUTO_STRUCTURE_PACKET = AUTO_STRUCTURE_PACKET;
        valueChanged.accept("AUTO_STRUCTURE_PACKET", AUTO_STRUCTURE_PACKET);
    }

    public void setHISTORY_SIZE(int HISTORY_SIZE) {
        this.HISTORY_SIZE = HISTORY_SIZE;
	    valueChanged.accept("HISTORY_SIZE", HISTORY_SIZE);
    }

    public void setDISPLAY_TIMESTAMP(boolean DISPLAY_TIMESTAMP) {
        this.DISPLAY_TIMESTAMP = DISPLAY_TIMESTAMP;
	    valueChanged.accept("DISPLAY_TIMESTAMP", DISPLAY_TIMESTAMP);
    }

    public void setALWAYS_BOTTOM_SCROLL(boolean ALWAYS_BOTTOM_SCROLL) {
        this.ALWAYS_BOTTOM_SCROLL = ALWAYS_BOTTOM_SCROLL;
	    valueChanged.accept("ALWAYS_BOTTOM_SCROLL", ALWAYS_BOTTOM_SCROLL);
    }

    public void setSERVER_BACKGROUND(Color SERVER_BACKGROUND) {
        this.SERVER_BACKGROUND = SERVER_BACKGROUND;
	    valueChanged.accept("SERVER_BACKGROUND", SERVER_BACKGROUND);
    }

    public void setCLIENT_BACKGROUND(Color CLIENT_BACKGROUND) {
        this.CLIENT_BACKGROUND = CLIENT_BACKGROUND;
	    valueChanged.accept("CLIENT_BACKGROUND", CLIENT_BACKGROUND);
    }

    public void setSSL_BACKGROUND(Color SSL_BACKGROUND) {
        this.SSL_BACKGROUND = SSL_BACKGROUND;
	    valueChanged.accept("SSL_BACKGROUND", SSL_BACKGROUND);
    }

    public void setCUSTOM_BACKGROUND(Color CUSTOM_BACKGROUND) {
        this.CUSTOM_BACKGROUND = CUSTOM_BACKGROUND;
	    valueChanged.accept("CUSTOM_BACKGROUND", CUSTOM_BACKGROUND);
    }

    public void setFAKE_FOREGROUND(Color FAKE_FOREGROUND) {
        this.FAKE_FOREGROUND = FAKE_FOREGROUND;
	    valueChanged.accept("FAKE_FOREGROUND", FAKE_FOREGROUND);
    }

    public void setREMEMBER_AUTH_PRIVACY_OPTION(boolean REMEMBER_AUTH_PRIVACY_OPTION) {
        this.REMEMBER_AUTH_PRIVACY_OPTION = REMEMBER_AUTH_PRIVACY_OPTION;
	    valueChanged.accept("REMEMBER_AUTH_PRIVACY_OPTION", REMEMBER_AUTH_PRIVACY_OPTION);
    }

    public void setSCRIPTS_DIRECTORY(File SCRIPTS_DIRECTORY) {
        this.SCRIPTS_DIRECTORY = SCRIPTS_DIRECTORY;
	    valueChanged.accept("SCRIPTS_DIRECTORY", SCRIPTS_DIRECTORY);
    }

    public void setAUTO_EXPAND_TREE(boolean AUTO_EXPAND_TREE) {
        this.AUTO_EXPAND_TREE = AUTO_EXPAND_TREE;
        valueChanged.accept("AUTO_EXPAND_TREE", AUTO_EXPAND_TREE);
    }

    public void setHIDE_AUTH(boolean HIDE_AUTH) {
        this.HIDE_AUTH = HIDE_AUTH;
	    valueChanged.accept("HIDE_AUTH", HIDE_AUTH);
    }

    public void setX(int x) {
        this.x = x;
	    valueChanged.accept("x", x);
    }

    public void setY(int y) {
        this.y = y;
	    valueChanged.accept("y", y);
    }

    public void setW(int w) {
        this.w = w;
	    valueChanged.accept("w", w);
    }

    public void setH(int h) {
        this.h = h;
	    valueChanged.accept("h", h);
    }

    public void setExtendedState(int extendedState) {
        this.extendedState = extendedState;
	    valueChanged.accept("extendedState", extendedState);
    }

    public void setSeparatorPacketListLocation(int separatorPacketListLocation) {
        this.separatorPacketListLocation = separatorPacketListLocation;
	    valueChanged.accept("separatorPacketListLocation", separatorPacketListLocation);
    }

    public void setSeparatorPacketViewLocation(int separatorPacketViewLocation) {
        this.separatorPacketViewLocation = separatorPacketViewLocation;
	    valueChanged.accept("separatorPacketViewLocation", separatorPacketViewLocation);
    }

    public void setRecentsFiles(String[] recentsFiles) {
        this.recentsFiles = recentsFiles;
	    valueChanged.accept("recentsFiles", recentsFiles);
    }

}
