package wakfulib.ui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import javax.swing.border.AbstractBorder;

/**
 * Bordure pour signaler les cellules dont la valeur est en train d'être modifiée.
 */
public class ModifiedValueBorder extends AbstractBorder {
    private final Color borderColour;
    private static final int SIZE_TRIANGE = 10;
    private static final int BORDER_INSERT = 1;

    public ModifiedValueBorder(Color colour) {
        borderColour = colour;
    }

    /**
     * {@inheritDoc}
     * Affiche un triangle rouge sur un composant.
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        Graphics2D g2d;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            g2d.setColor(borderColour);
            g2d.fill(new TriangleShape(new Point2D.Double(width, 0),
                    new Point2D.Double(width, SIZE_TRIANGE), new Point2D.Double(width - SIZE_TRIANGE, 0)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(BORDER_INSERT, BORDER_INSERT, BORDER_INSERT, BORDER_INSERT)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = BORDER_INSERT;
        return insets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    /**
     * Classe représentant un triangle
     */
    static class TriangleShape extends Path2D.Double {
        /**
         * Creer un triangle à partir de 3 points
         */
        public TriangleShape(Point2D... points) {
            assert points.length == 3;
            moveTo(points[0].getX(), points[0].getY());
            lineTo(points[1].getX(), points[1].getY());
            lineTo(points[2].getX(), points[2].getY());
            closePath();
        }
    }
}
