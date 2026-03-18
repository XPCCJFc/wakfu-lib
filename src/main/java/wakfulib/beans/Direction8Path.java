package wakfulib.beans;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import wakfulib.doc.NonNull;
import wakfulib.logic.OutPacket;
import wakfulib.utils.data.Point3i;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of movements (steps) in the game world.
 * Each step consists of a direction and a height difference.
 */
@ToString
public class Direction8Path {
    private final List<Step> steps;

    /**
     * Creates an empty path.
     */
    public Direction8Path() {
        steps = new ArrayList<>();
    }

    /**
     * Creates a path containing a single step in the specified direction.
     *
     * @param direction8 The direction for the single step.
     * @return A new path with one step.
     */
    @NonNull
    public static Direction8Path uniqueStepPath(@NonNull Direction8 direction8) {
        Direction8Path direction8Path = new Direction8Path();
        direction8Path.addSimpleStep(direction8);
        return direction8Path;
    }

    /**
     * Adds a step to the path.
     *
     * @param step The step to add.
     */
    public void addStep(@NonNull Step step) {
        steps.add(step);
    }

    /**
     * Adds a step in the specified direction with no height difference.
     *
     * @param direction8 The direction of the step.
     */
    public void addSimpleStep(@NonNull Direction8 direction8) {
        steps.add(new Step(direction8, 0));
    }

    /**
     * Encodes this path into an outgoing packet.
     *
     * @param o The packet to write to.
     */
    public void encode(@NonNull OutPacket o) {
        o.write((byte) steps.size());
        for (Step step : steps) {
            int data = (step.direction.id & 7) << 5;
            data |= step.heightDiff & 31;
            o.write((byte)data);
        }
    }

    /**
     * Calculates the final position after following this path from a starting position.
     * The starting position object is modified directly.
     *
     * @param startingPosition The initial position.
     */
    public void getEndPosition(@NonNull Point3i startingPosition) {
        for (Step step : steps) {
            step.apply(startingPosition);
        }
    }

    /**
     * Decodes a path from a byte buffer.
     *
     * @param bb The buffer to read from.
     * @return The decoded path.
     */
    @NonNull
    public static Direction8Path decode(@NonNull ByteBuffer bb) {
        Direction8Path res = new Direction8Path();
        int size = (int) bb.get();
        for (int i = 0; i < size; i++) {
            res.addStep(new Step(bb.get()));
        }
        return res;
    }

    /**
     * Returns the number of steps in the path.
     *
     * @return The number of steps.
     */
    public int size() {
        return steps.size();
    }

    /**
     * Removes the last step from the path.
     */
    public void removeLastStep() {
        steps.remove(steps.size() - 1);
    }

    /**
     * Utility class for handling individual cell steps and their serialization.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CellStep {

        /**
         * Decodes a 3D point from a buffer.
         *
         * @param bb The buffer to read from.
         * @return The decoded point.
         */
        public static Point3i unserialize(@NonNull ByteBuffer bb) {
            return new Point3i(bb.getInt(), bb.getInt(), bb.getShort());
        }

        /**
         * Encodes a 3D point into a packet.
         *
         * @param cellStep The point to encode.
         * @param o The packet to write to.
         */
        public static void serialize(@NonNull Point3i cellStep, @NonNull OutPacket o) {
            o.writeInt(cellStep.getX());
            o.writeInt(cellStep.getY());
            o.writeShort(cellStep.getZ());
        }

        /**
         * Determines the direction from one cell to an adjacent cell.
         *
         * @param lastCell The current cell.
         * @param nextCell The next cell.
         * @return The direction to move from lastCell to nextCell.
         */
        public static Direction8 getDirection8(@NonNull Point3i lastCell, @NonNull Point3i nextCell) {
            return Direction8.of(nextCell.getX() - lastCell.getX(), nextCell.getY() - lastCell.getY());
        }
    }

    /**
     * Represents a single movement in a path.
     */
    @ToString
    public static class Step {
        public final Direction8 direction;
        public final int heightDiff;

        /**
         * Creates a new step.
         *
         * @param direction The direction of the step.
         * @param heightDiff The height difference of the step.
         */
        public Step(@NonNull Direction8 direction, int heightDiff) {
            this.direction = direction;
            this.heightDiff = heightDiff;
        }

        /**
         * Decodes a step from a raw byte.
         *
         * @param data The encoded step data.
         */
        public Step(byte data) {
            direction = Direction8.getDirectionFromIndex(data >> 5 & 7);
            int heightDiffTemp = data & 31;
            if ((heightDiffTemp & 16) != 0) {
                heightDiffTemp |= -32;
            }
            heightDiff = heightDiffTemp;
        }

        /**
         * Applies this step to a position.
         *
         * @param startingPosition The position to modify.
         */
        public void apply(@NonNull Point3i startingPosition) {
            startingPosition.setX(startingPosition.getX() + direction.x);
            startingPosition.setY(startingPosition.getY() + direction.y);
            startingPosition.setZ((short) (startingPosition.getZ() + heightDiff));
        }
    }
}
