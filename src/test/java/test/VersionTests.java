package test;

import org.junit.Assert;
import org.junit.Test;
import wakfulib.internal.Version;

public class VersionTests {

    @Test
    public void testRangeVersion() {
        Assert.assertTrue(Version.v1_66_1.isInRange(Version.v1_66_1, Version.UNKNOWN));
        Assert.assertTrue(Version.v1_66_1.isInRange(Version.v1_65_2, Version.UNKNOWN));
        Assert.assertFalse(Version.v1_65_2.isInRange(Version.v1_66_1, Version.UNKNOWN));

        Assert.assertTrue(Version.v1_66_1.isInRange(Version.v1_66_1, Version.v1_66_1));
        Assert.assertFalse(Version.v1_66_1.isInRange(Version.v1_65_2, Version.v1_65_2));
        Assert.assertFalse(Version.v1_66_1.isInRange(Version.UNKNOWN, Version.v1_65_2));
        Assert.assertTrue(Version.v1_66_1.isInRange(Version.UNKNOWN, Version.v1_66_1));
        Assert.assertTrue(Version.v1_66_1.isInRange(Version.v1_65_2, Version.v1_66_1));
        Assert.assertTrue(Version.v1_65_2.isInRange(Version.v1_65_2, Version.v1_65_2));
        Assert.assertTrue(Version.v1_65_2.isInRange(Version.v1_65_2, Version.v1_66_1));
        Assert.assertTrue(Version.v1_65_2.isInRange(Version.v1_63_2, Version.v1_66_1));
    }

    @Test
    public void testVersionComparaison() {
      Assert.assertTrue(Version.v1_65_2.isNewerThan(Version.v0_315));
      Assert.assertTrue(Version.v1_65_2.isNewerThan(Version.v1_63_0));
      Assert.assertFalse(Version.v1_65_2.isNewerThan(Version.v1_65_2));

      Assert.assertTrue(Version.v0_315.isOlderThan(Version.v1_65_2));
      Assert.assertTrue(Version.v1_63_0.isOlderThan(Version.v1_65_2));
      Assert.assertFalse(Version.v1_65_2.isOlderThan(Version.v1_65_2));
    }
}
