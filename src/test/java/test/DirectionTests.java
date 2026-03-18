package test;

import org.junit.Assert;
import org.junit.Test;
import wakfulib.beans.Direction8;

public class DirectionTests {
    
    @Test
    public void testOpposite() {
        var directions = new Direction8[]{
            Direction8.NORTH, Direction8.SOUTH,
            Direction8.NORTH_EAST, Direction8.SOUTH_WEST,
            Direction8.EAST, Direction8.WEST,
            Direction8.NORTH_WEST, Direction8.SOUTH_EAST,
        };

        var values = Direction8.values();
        
        for (int i = 0; i < directions.length; i = i + 2) {
            var dir1 = directions[i];
            var dir2 = directions[i + 1];
            Assert.assertTrue(dir2 + " & " + dir1 + " are not opposite", Direction8.isOpposite(dir2, dir1));
            Assert.assertTrue(dir1 + " & " + dir2 + " are not opposite", Direction8.isOpposite(dir1, dir2));
            for (Direction8 value : values) {
                if (value != dir2) {
                    Assert.assertFalse(dir1 + " & " + value + " are opposite", Direction8.isOpposite(dir1, value));
                }
                if (value != dir1) {
                    Assert.assertFalse(dir2 + " & " + value + " are opposite", Direction8.isOpposite(dir2, value));
                }
            }
        }

        for (Direction8 value : values) {
            var dir2 = value.opposite();
            Assert.assertTrue(value + " & " + dir2 + " are opposite", Direction8.isOpposite(value, dir2));
        }
    }
    
    @Test
    public void testFromDelta() {
        for (Direction8 value : Direction8.DIRECTION_8_VALUES) {
            Assert.assertEquals(value, Direction8.of(value.x, value.y));
        }
        
    }
    
    @Test
    public void testDir4() {
        Direction8[] directions = new Direction8[] {
            Direction8.SOUTH_EAST, Direction8.SOUTH_EAST,
            Direction8.SOUTH_WEST, Direction8.SOUTH_WEST,
            Direction8.NORTH_WEST, Direction8.NORTH_WEST,
            Direction8.NORTH_EAST, Direction8.NORTH_EAST,
            Direction8.EAST, Direction8.SOUTH_EAST, 
            Direction8.WEST, Direction8.NORTH_WEST, 
            Direction8.NORTH, Direction8.NORTH_EAST, 
            Direction8.SOUTH, Direction8.SOUTH_WEST
        };
        
        for (int i = 0; i < directions.length; i = i + 2) {
            var dir1 = directions[i];
            var dir2 = directions[i + 1];
            Assert.assertEquals(dir1 + ".toDirection4() != " + dir2, dir2, dir1.toDirection4());
        }
    }

    @Test
    public void testIsDir4NotDiago() {
        for (Direction8 value : Direction8.values()) {
            Assert.assertNotEquals("For direction " + value, value.isDirection4(), value.isDiagonal() || value.isZ());
        }
    }
}
