package wakfulib.ui.utils;

import java.awt.Color;

public class RGB {

    public static Color newColorWithAlpha(Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
    }
}
