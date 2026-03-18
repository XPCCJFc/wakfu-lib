package wakfulib.utils.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a 3D coordinate point (x, y, z) in the game world.
 * Provides utility methods for position manipulation, distance calculations, and serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Point3i {
    private int x;
    private int y;
    private short z;

    /**
     * Creates a new point with the specified x and y coordinates, and z defaulting to 0.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Point3i(int x, int y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    /**
     * Creates a new point from an array of coordinates [x, y, z].
     *
     * @param coords An array containing at least 3 elements.
     */
    public Point3i(int[] coords) {
        this.x = coords[0];
        this.y = coords[1];
        this.z = (short)coords[2];
    }

    /**
     * Copy constructor.
     *
     * @param p The point to copy.
     */
    public Point3i(Point3i p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    /**
     * Updates the coordinates of this point.
     *
     * @param x The new X coordinate.
     * @param y The new Y coordinate.
     * @param z The new Z coordinate.
     */
    public void update(int x, int y, short z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Adds a vector to the current coordinates.
     *
     * @param vector3i The vector to add.
     */
    public void update(Vector3i vector3i) {
        this.x = this.x + vector3i.getX();
        this.y = this.y + vector3i.getY();
        this.z = (short) (this.z + vector3i.getZ());
    }

    /**
     * Copies the coordinates from another point.
     *
     * @param other The point to copy from.
     */
    public void update(Point3i other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    /**
     * Calculates the 2D Euclidean distance to another point, ignoring the Z axis.
     *
     * @param other The point to calculate the distance to.
     * @return The 2D distance.
     */
    public double distance2D(Point3i other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
    }

    /**
     * Calculates the 3D Euclidean distance to another point.
     *
     * @param other The point to calculate the distance to.
     * @return The 3D distance.
     */
    public double distance(Point3i other) {
        int dx = x - other.x;
        int dy = y - other.y;
        int dz = z - other.z;
        return Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0) + Math.pow(dz, 2.0));
    }

    /**
     * Packs the coordinates into a single long value for compact storage or network transmission.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     * @return The packed long representation.
     * @throws RuntimeException if coordinates are outside the supported range.
     */
    public static long toLong(int x, int y, short z) {
        long ux = (long)x + 131071L & 262143L;
        long uy = (long)y + 131071L & 262143L;
        long uz = (long)z + 511L & 1023L;
        if (Math.abs(x) <= 131072 - (x < 0 ? 1 : 0) && Math.abs(y) <= 131072 - (y < 0 ? 1 : 0) && Math.abs(z) <= 512 - (z < 0 ? 1 : 0)) {
            return ux << 28 | uy << 10 | uz;
        } else {
            throw new RuntimeException("Paramètres d'une position en dehors de la map - position : " + x + ", " + y + ", " + z);
        }
    }

    /**
     * Packs the point's coordinates into a single long value.
     *
     * @param pos The point to pack.
     * @return The packed long representation.
     */
    public static long toLong(Point3i pos) {
        return toLong(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Unpacks a long value into a new Point3i instance.
     *
     * @param value The packed long value.
     * @return A new Point3i instance with the unpacked coordinates.
     */
    public static Point3i fromLong(long value) {
        short z = (short)((int)((value & 1023L) - 511L));
        int y = (int)((value >> 10 & 262143L) - 131071L);
        int x = (int)((value >> 28 & 262143L) - 131071L);
        return new Point3i(x, y, z);
    }

    /**
     * Checks if two points have the same X and Y coordinates, ignoring altitude (Z).
     *
     * @param point3 The point to compare with.
     * @return {@code true} if X and Y match, {@code false} otherwise.
     */
    public boolean equalsIgnoringAltitude(Point3i point3) {
        if (this == point3) {
            return true;
        } else if (point3 != null) {
            return this.x == point3.x && this.y == point3.y;
        } else {
            return false;
        }
    }

    /**
     * Checks if the point matches the specified X and Y coordinates.
     *
     * @param x The X coordinate to check.
     * @param y The Y coordinate to check.
     * @return {@code true} if X and Y match, {@code false} otherwise.
     */
    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    /**
     * Creates a copy of this point.
     *
     * @return A new Point3i instance with the same coordinates.
     */
    public Point3i clone() {
        return new Point3i(this.x, this.y, this.z);
    }

}
