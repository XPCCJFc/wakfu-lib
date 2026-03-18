package test;

import org.junit.Assert;
import org.junit.Test;
import wakfulib.utils.MathHelper;

public class MathHelperTest {

    @Test
    public void testIntToShortConversion() {
        var testSuite = new short[] {
            0, 0,
            2, 2,
            Short.MIN_VALUE, Short.MAX_VALUE,
            Short.MAX_VALUE, Short.MIN_VALUE,
            10, Short.MAX_VALUE,
            Short.MAX_VALUE, 10,
            10, -10,
            -10, 10,
            0, -10,
            -10, 0,
            0, Short.MAX_VALUE,
            Short.MAX_VALUE, 0,
            0, Short.MIN_VALUE,
            Short.MIN_VALUE, 0,
        };
        for (int i = 0; i < testSuite.length; i++) {
            short s1 = testSuite[i++], s2 = testSuite[i];
            var r = MathHelper.getIntFromTwoShort(s1, s2);

            String msg = "At " + s1 + ", " + s2;
            Assert.assertEquals(msg, s1, MathHelper.getFirstShortFromInt(r));
            Assert.assertEquals(msg, s2, MathHelper.getSecondShortFromInt(r));
        }
    }
}
