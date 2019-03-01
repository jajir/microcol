package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CalendarSeasonTest {

    private final static int START_YEAR = 1492;

    private final static int END_YEAR = 1800;

    static Stream<Arguments> dataProvider() {
        return Stream.of(arguments(1492, null, 0), arguments(1599, null, 107),
                arguments(1600, Calendar.Season.spring, 108),
                arguments(1600, Calendar.Season.summer, 109),
                arguments(1600, Calendar.Season.autumn, 110),
                arguments(1600, Calendar.Season.winter, 111),
                arguments(1601, Calendar.Season.spring, 112),
                arguments(1626, Calendar.Season.spring, 212),
                arguments(1603, Calendar.Season.winter, 123));
    }

    @ParameterizedTest(name = "{index}: startYear = {0}, endYear = {1}")
    @MethodSource("dataProvider")
    public void testCreation(final int currentYear, final Calendar.Season season,
            final int turnNo) {
        final Calendar calendar = new Calendar(START_YEAR, END_YEAR, turnNo);
        assertEquals(START_YEAR, calendar.getStartYear(), "Test of startYear failed:");
        assertEquals(END_YEAR, calendar.getEndYear(), "Test of endYear failed:");
        assertFalse(calendar.isFinished(), "Test of isFinished() failed:");

        assertEquals(currentYear, calendar.getCurrentYear(), "Test of currentYear failed:");
        if (season == null) {
            assertFalse(calendar.getCurrentSeason().isPresent(), "Test of currentSeason failed:");
        } else {
            assertEquals(season, calendar.getCurrentSeason().get(),
                    "Test of currentSeason failed:");
        }
    }
}
