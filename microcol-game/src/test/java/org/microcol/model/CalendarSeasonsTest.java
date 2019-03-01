package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalendarSeasonsTest {

    @ParameterizedTest(name = "{index}: startYear = {0}, endYear = {1}")
    @CsvSource({ "1590, 1750", "1590, 1591", "0, 1", "-1, 0", "-1590, -1589", "-1750, -1590" })
    public void testCreation(int startYear, int endYear) {
        final Calendar calendar = new Calendar(startYear, endYear, 0);

        assertEquals(startYear, calendar.getStartYear(), "Test of startYear failed:");
        assertEquals(endYear, calendar.getEndYear(), "Test of endYear failed:");
        assertEquals(startYear, calendar.getCurrentYear(), "Test of currentYear failed:");
        assertFalse(calendar.isFinished(), "Test of isFinished() failed:");
    }
}
