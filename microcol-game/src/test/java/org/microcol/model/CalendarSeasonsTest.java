package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalendarSeasonsTest {

    @ParameterizedTest(name = "start {0}, round {1}: {2} {3}")
    @CsvSource({ "1490, 0, 1490,", "1490, 109, 1599,",
            "1490, 110, 1600, spring", "1490, 111, 1600, summer",
            "1490, 112, 1600, autumn", "1490, 113, 1600, winter",
            "1490, 114, 1601, spring", "1600, 0, 1600, spring",
            "1600, 1, 1600, summer", "1600, 4, 1601, spring",
            "1630, 0, 1630, spring", "1630, 1, 1630, summer",
            "1630, 2, 1630, autumn", "1630, 3, 1630, winter",
            "1630, 4, 1631, spring" })
    void referenceAndLateStartsAdvanceToTheExpectedDate(final int startYear,
            final int round, final int expectedYear,
            final Calendar.Season expectedSeason) {
        final Calendar calendar = new Calendar(startYear, 1850,
                Math.max(0, round - 1));
        if (round > 0) {
            calendar.endRound();
        }
        assertEquals(round, calendar.getNumberOfPlayedTurns());
        assertEquals(expectedYear, calendar.getCurrentYear());
        assertEquals(expectedSeason, calendar.getCurrentSeason().orElse(null));
    }

    @ParameterizedTest(name = "{index}: startYear = {0}, endYear = {1}")
    @CsvSource({ "1590, 1750", "1590, 1591", "0, 1", "-1, 0", "-1590, -1589",
            "-1750, -1590" })
    public void testCreation(int startYear, int endYear) {
        final Calendar calendar = new Calendar(startYear, endYear, 0);

        assertEquals(startYear, calendar.getStartYear(),
                "Test of startYear failed:");
        assertEquals(endYear, calendar.getEndYear(), "Test of endYear failed:");
        assertEquals(startYear, calendar.getCurrentYear(),
                "Test of currentYear failed:");
        assertFalse(calendar.isFinished(), "Test of isFinished() failed:");
    }
}
