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
public class CalendarSeasonTest {
    
    private final static int START_YEAR = 1492;
    
    private final static int END_YEAR = 1800;

    @Parameters(name = "{index}: startYear = {0}, endYear = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 1492, null, 0},
            { 1599, null, 107 },
            { 1600, Calendar.Season.spring, 108 },
            { 1600, Calendar.Season.summer, 109 },
            { 1600, Calendar.Season.autumn, 110 },
            { 1600, Calendar.Season.winter, 111 },
            { 1601, Calendar.Season.spring, 112 },
            { 1626, Calendar.Season.spring, 212 },
            { 1603, Calendar.Season.winter, 123 },
        });
    }

    @Parameter(0)
    public int currentYear;

    @Parameter(1)
    public Calendar.Season season;
    
    @Parameter(2)
    public int turnNo;

    @Test
    public void testCreation() {
	final Calendar calendar = new Calendar(START_YEAR, END_YEAR, turnNo);
	Assert.assertEquals("Test of startYear failed:", START_YEAR, calendar.getStartYear());
	Assert.assertEquals("Test of endYear failed:", END_YEAR, calendar.getEndYear());
	Assert.assertFalse("Test of isFinished() failed:", calendar.isFinished());

	Assert.assertEquals("Test of currentYear failed:", currentYear, calendar.getCurrentYear());
	if (season == null) {
	    Assert.assertFalse("Test of currentSeason failed:",
		    calendar.getCurrentSeason().isPresent());
	} else {
	    Assert.assertEquals("Test of currentSeason failed:", season,
		    calendar.getCurrentSeason().get());
	}
    }
}
