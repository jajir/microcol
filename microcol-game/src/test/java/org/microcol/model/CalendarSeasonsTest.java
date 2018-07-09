package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CalendarSeasonsTest {

    @Parameters(name = "{index}: startYear = {0}, endYear = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 1590, 1750 },
            { 1590, 1591 },
            { 0, 1 },
            { -1, 0 },
            { -1590, -1589 },
            { -1750, -1590 },
        });
    }

    @Parameter(0)
    public int startYear;

    @Parameter(1)
    public int endYear;

    @Test
    public void testCreation() {
        final Calendar calendar = new Calendar(startYear, endYear, 0);

        Assert.assertEquals("Test of startYear failed:", startYear, calendar.getStartYear());
        Assert.assertEquals("Test of endYear failed:", endYear, calendar.getEndYear());
        Assert.assertEquals("Test of currentYear failed:", startYear, calendar.getCurrentYear());
        Assert.assertFalse("Test of isFinished() failed:", calendar.isFinished());
    }
}
