package test;

import org.junit.Assert;
import org.junit.Test;
import wakfulib.utils.random.DiceRoll;

public class DiceRollTest {
    
    @Test
    public void testRollInt() {
        int[] values = new int[6];
        for (int i = 0; i < 1000; i++) {
            values[DiceRoll.roll(6) - 1]++;
        }
        for (int i = 0; i < values.length; i++) {
            System.out.println(i + " -> " + values[i]);
        }

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(1, DiceRoll.roll(-1));
            Assert.assertEquals(1, DiceRoll.roll(0));
        }
        
    }

    @Test
    public void testRollLong() {
        long[] values = new long[6];
        for (long i = 0; i < 1000; i++) {
            values[(int) (DiceRoll.roll(6L) - 1)]++;
        }
        for (int i = 0; i < values.length; i++) {
            System.out.println(i + " -> " + values[i]);
        }

        for (long i = 0; i < 100; i++) {
            Assert.assertEquals(1, DiceRoll.roll(-1L));
            Assert.assertEquals(1, DiceRoll.roll(0L));
        }
    }
    
    @Test
    public void testRollRange() {
        Assert.assertEquals(0, DiceRoll.roll(0, 0));
        Assert.assertEquals(0, DiceRoll.roll(0, -1));
        Assert.assertEquals(-1, DiceRoll.roll(-1, 0));
        for (int i = 0; i < 1000; i++) {
            var min = 3;
            var max = 5;
            var res = DiceRoll.roll(min, max);
            Assert.assertTrue("res >= min && res <= max = false with res = " + res + ", min = " + min + ", max = " + max,
                res >= min && res <= max);
        }
    }
    
    
}
