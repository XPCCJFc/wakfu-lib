package wakfulib.utils.data;

import lombok.Getter;
import wakfulib.beans.Direction8;

/**
 * Represents a 3D vector with integer components. 
 * Commonly used for position offsets or movement vectors in the game world.
 */
@Getter
public class Vector3i {
    private int x;
    private int y;
    private int z;

    /**
     * Creates a new vector at the origin (0, 0, 0).
     */
    public Vector3i() {
        this(0, 0, 0);
    }

    /**
     * Creates a new vector with the specified components.
     *
     * @param x the X component
     * @param y the Y component
     * @param z the Z component
     */
    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Creates a new vector representing the displacement between two points.
     *
     * @param startPoint the starting point
     * @param endPoint the end point
     */
    public Vector3i(Point3i startPoint, Point3i endPoint) {
        this.x = endPoint.getX() - startPoint.getX();
        this.y = endPoint.getY() - startPoint.getY();
        this.z = endPoint.getZ() - startPoint.getZ();
    }

    /**
     * Sets the components of the vector.
     *
     * @param coords an array of 3 integers representing X, Y, and Z
     */
    public void set(int[] coords) {
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
    }

    /**
     * Converts the X and Y components of this vector into its nearest 
     * primary 4-way direction in the isometric grid.
     *
     * @return the corresponding 4-way direction
     */
    public Direction8 toDirection4() {
        return getDirection4FromVector(this.x, this.y);
    }

    /**
     * Resolves a 4-way direction from raw X and Y vector components.
     * Uses trigonometric analysis of the displacement vector.
     *
     * @param vx the X displacement
     * @param vy the Y displacement
     * @return the nearest 4-way direction
     */
    public static Direction8 getDirection4FromVector(double vx, double vy) {
        double a = Math.atan(Math.abs(vy) / Math.abs(vx));
        if (vx < 0.0) {
            a = Math.PI - a;
        }

        if (vy > 0.0) {
            a = -a;
        }

        Direction8 direction;
        if (a <= 2.356194490192345 && a >= 0.7853981633974483) {
            direction = Direction8.NORTH_EAST;
        } else if (a <= 0.7853981633974483 && a >= -0.7853981633974483) {
            direction = Direction8.SOUTH_EAST;
        } else if (a <= -0.7853981633974483 && a >= -2.356194490192345) {
            direction = Direction8.SOUTH_WEST;
        } else {
            direction = Direction8.NORTH_WEST;
        }

        return direction;
    }

    /**
     * Converts the X and Y components of this vector into its nearest 
     * 8-way direction in the isometric grid.
     *
     * @return the corresponding 8-way direction
     */
    public Direction8 toDirection8() {
        return getDirection8FromVector(this.x, this.y);
    }

    /**
     * Resolves an 8-way direction from raw X and Y vector components.
     * Uses trigonometric analysis of the displacement vector.
     *
     * @param vx the X displacement
     * @param vy the Y displacement
     * @return the nearest 8-way direction
     */
    public static Direction8 getDirection8FromVector(double vx, double vy) {
        double a = Math.atan(Math.abs(vy) / Math.abs(vx));
        if (vx < 0.0) {
            a = Math.PI - a;
        }
        if (vy > 0.0) {
            a = -a;
        }
        return a <= 2.748893571891069 && a >= 1.9634954084936207 ? Direction8.NORTH : (a <= 1.9634954084936207 && a >= 1.1780972450961724 ? Direction8.NORTH_EAST : (a <= 1.1780972450961724 && a >= 0.39269908169872414 ? Direction8.EAST : (a <= 0.39269908169872414 && a >= -0.39269908169872414 ? Direction8.SOUTH_EAST : (a <= -0.39269908169872414 && a >= -1.1780972450961724 ? Direction8.SOUTH : (a <= -1.1780972450961724 && a >= -1.9634954084936207 ? Direction8.SOUTH_WEST : (a <= -1.9634954084936207 && a >= -2.748893571891069 ? Direction8.WEST : Direction8.NORTH_WEST))))));
    }
}
