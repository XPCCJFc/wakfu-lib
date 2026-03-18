package wakfulib.ui.tv.porst.splib.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Helper class that contains GUI functions.
 */
public final class GuiHelpers {

    private static String SYSTEM_MONOSPACED_FONT;

	/**
	 * Returns the system monospace font.
	 *
	 * @return The name of the system monospace font.
	 */
	public static String getMonospaceFont() {
        if (SYSTEM_MONOSPACED_FONT != null) return SYSTEM_MONOSPACED_FONT;
		final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Font[] fonts = localGraphicsEnvironment.getAllFonts();
		for (Font font : fonts) {
			if (font.getName().equals("Courier New")) {
				return (SYSTEM_MONOSPACED_FONT = "Courier New");
			}
		}
		return (SYSTEM_MONOSPACED_FONT = "Monospaced");
	}
}
