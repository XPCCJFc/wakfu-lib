package wakfulib.ui.utils;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class IconUtils {

    private static Map<String, BufferedImage> CACHE = new HashMap<>();
    
    @Nullable
    public static ImageIcon loadIcon(String iconName) {
        var image = loadImage(iconName);
        if (image == null) {
            return null;
        } else {
            return antiAliased(image);
        }
    }
    
    public static ImageIcon antiAliased(BufferedImage image) {
        return new ImageIcon(image) {
            @Override
            public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                if (g instanceof Graphics2D g2d) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                }
                super.paintIcon(c, g, x, y);
            }
        };
    }

    @Nullable
    public static BufferedImage loadImage(String iconName) {
        var hit = CACHE.get(iconName);
        if (hit != null) {
            return hit;
        } else {
            try (InputStream resourceAsStream = JMenuItemBuilder.class.getResourceAsStream(iconName)) {
                if (resourceAsStream == null) {
                    log.error("Icon {} not found !", iconName);
                    return null;
                }
                var res = ImageIO.read(resourceAsStream);
                CACHE.put(iconName, res);
                return res;
            } catch (IOException e) {
                log.error("Failed to load icon {}", iconName, e);
            }
            return null;
        }
    }

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    @Nullable
    public static BufferedImage rotate(@Nullable BufferedImage image, double degrees) {
        if (image == null) return null;
        image = copyImage(image);
        // Calculate the new size of the image based on the angle of rotation
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.round(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.round(image.getWidth() * sin + image.getHeight() * cos);

        // Create a new image
        BufferedImage rotate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotate.createGraphics();
        // Calculate the "anchor" point around which the image will be rotated
        int x = (newWidth - image.getWidth()) / 2;
        int y = (newHeight - image.getHeight()) / 2;
        // Transform the origin point around the anchor point
        AffineTransform at = new AffineTransform();
        at.setToRotation(radians, x + (image.getWidth() / 2), y + (image.getHeight() / 2));
        at.translate(x, y);
        g2d.setTransform(at);
        // Paint the originl image
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rotate;
    }
}
