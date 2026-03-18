package wakfulib.beans;

import wakfulib.doc.NonNull;

/**
 * Enumeration representing the eight cardinal and ordinal directions in the Wakfu world.
 * Also includes special directions like NONE, TOP, and BOTTOM.
 */
public enum Direction8 {
    /** East (0, 1, -1) */
    EAST(0, 1, - 1),
    /** South-East (1, 1, 0) */
    SOUTH_EAST(1, 1, 0),
    /** South (2, 1, 1) */
    SOUTH(2, 1, 1),
    /** South-West (3, 0, 1) */
    SOUTH_WEST(3, 0, 1),
    /** West (4, - 1, 1) */
    WEST(4, - 1, 1),
    /** North-West (5, - 1, 0) */
    NORTH_WEST(5, - 1, 0),
    /** North (6, - 1, - 1) */
    NORTH(6, - 1, - 1),
    /** North-East (7, 0, - 1) */
    NORTH_EAST(7, 0, - 1),
    /** No direction (0, 0, 0) */
    NONE(-90, 0, 0),
    /** Upward vertical direction (0, 0, 0) */
    TOP(-95, 0, 0),
    /** Downward vertical direction (0, 0, 0) */
    BOTTOM(-99, 0, 0);

    /** The internal ID of the direction, used in network protocols. */
    public final int id;
    /** The X-coordinate component of the direction vector. */
    public final int x;
    /** The Y-coordinate component of the direction vector. */
    public final int y;
    /** Whether this is a primary (non-diagonal) 4-way direction. */
    public final boolean dir4;

    private final int[] vector;

    private static final Direction8[] VALUES = values();
    /** Array containing all 8 standard cardinal and ordinal directions. */
    public static Direction8[] DIRECTION_8_VALUES = new Direction8[]{SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST, EAST, WEST, NORTH, SOUTH};
    /**
     * Array containing only the 4 diagonal directions in the isometric view.
     * These are the primary movement directions in the Wakfu world.
     */
    public static Direction8[] DIRECTION_4_VALUES = new Direction8[]{SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST};

    Direction8(int index, int x, int y) {
        this.id = index;
        this.x = x;
        this.y = y;
        vector = new int[]{x, y};
        dir4 = Math.abs(x) + Math.abs(y) == 1;
    }

    /**
     * Resolves a direction from its internal protocol ID.
     *
     * @param index The internal ID used in network messages.
     * @return The corresponding Direction8, or {@code NONE} if the ID is not recognized.
     */
    @NonNull
    public static Direction8 getDirectionFromIndex(int index) {
        for (Direction8 direction : Direction8.VALUES) {
            if (direction.id == index) {
                return direction;
            }
        }
        return Direction8.NONE;
    }

    /**
     * Resolves a direction from its X and Y vector components in the isometric grid.
     *
     * @param difX The X component of the displacement vector.
     * @param difY The Y component of the displacement vector.
     * @return The corresponding Direction8, or {@code NONE} if no match is found.
     */
    @NonNull
    public static Direction8 of(int difX, int difY) {
        for (Direction8 direction : Direction8.VALUES) {
            if (direction.x == difX && direction.y == difY) {
                return direction;
            }
        }
        return Direction8.NONE;
    }

    /**
     * Returns the direction vector as a 2D array [x, y] in the isometric grid.
     *
     * @return The direction vector components.
     */
    @NonNull
    public int[] getVector() {
        return vector;
    }

    /**
     * Checks if this is a primary movement direction in the isometric grid.
     *
     * @return {@code true} if it's a primary movement direction, {@code false} otherwise.
     */
    public boolean isDirection4() {
        return dir4;
    }

    /**
     * Checks if this is a diagonal direction relative to the isometric grid axes.
     *
     * @return {@code true} if it's diagonal, {@code false} otherwise.
     */
    public boolean isDiagonal() {
        return this.vector[0] != 0 && this.vector[1] != 0;
    }

    /**
     * Checks if this is a vertical or zero-displacement direction.
     *
     * @return {@code true} if the direction does not involve horizontal or vertical movement on the grid.
     */
    public boolean isZ() {
        return this.vector[0] == 0 && this.vector[1] == 0;
    }

    /**
     * Converts this direction to its nearest primary 4-way movement equivalent.
     *
     * @return The closest primary direction.
     */
    @NonNull
    public Direction8 toDirection4() {
        return isDirection4() ? this : VALUES[id + 1];
    }

    /**
     * Extracts the horizontal component of this direction.
     *
     * @return The horizontal direction component.
     */
    @NonNull
    public Direction8 getHorizontalDirection() {
        switch (this) {
            case EAST:
            case SOUTH_EAST:
            case SOUTH:
                return SOUTH_EAST;
            case SOUTH_WEST:
            default:
                return NONE;
            case WEST:
            case NORTH_WEST:
            case NORTH:
                return NORTH_WEST;
        }
    }

    /**
     * Extracts the vertical component of this direction.
     *
     * @return The vertical direction component.
     */
    @NonNull
    public Direction8 getVerticalDirection() {
        switch (this) {
            case EAST:
            case NORTH:
            case NORTH_EAST:
                return NORTH_EAST;
            case SOUTH_EAST:
            case NORTH_WEST:
            default:
                return NONE;
            case SOUTH:
            case SOUTH_WEST:
            case WEST:
                return SOUTH_WEST;
        }
    }

    /**
     * Determines if two directions are exact opposites (180 degrees apart).
     *
     * @param direction1 The first direction.
     * @param direction2 The second direction.
     * @return {@code true} if they are opposite, {@code false} otherwise.
     */
    public static boolean isOpposite(@NonNull Direction8 direction1, @NonNull Direction8 direction2) {
        if (direction1 == NONE) {
            return direction2 == NONE;
        }
        return Math.abs(direction1.id - direction2.id) == 4;
    }

    /**
     * Calculates the direction exactly opposite to this one.
     *
     * @return The opposite direction.
     */
    @NonNull
    public Direction8 opposite() {
        switch (this) {
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case TOP:
                return BOTTOM;
            case BOTTOM:
                return TOP;
            default:
                return NONE;
        }
    }
}
