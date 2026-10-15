package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.CalendarPo;

public class CalendarTest {

    @Test
    void historicalQueriesUseSeasonsWithoutChangingTheCurrentDate() {
        final Calendar calendar = new Calendar(1490, 1850, 110);
        assertEquals(1490, calendar.getYearForTurnNo(0));
        assertEquals(1599, calendar.getYearForTurnNo(109));
        assertEquals(1600, calendar.getYearForTurnNo(110));
        assertEquals(1600, calendar.getYearForTurnNo(113));
        assertEquals(1601, calendar.getYearForTurnNo(114));
        assertEquals(110, calendar.getNumberOfPlayedTurns());
        assertEquals(1600, calendar.getCurrentYear());
        assertEquals(Calendar.Season.spring, calendar.getCurrentSeason().get());
    }

    @Test
    void lateStartHistoricalQueriesUseTheStoredStart() {
        final Calendar calendar = new Calendar(1630, 1850, 0);
        assertEquals(1630, calendar.getYearForTurnNo(1));
        assertEquals(1630, calendar.getYearForTurnNo(3));
        assertEquals(1631, calendar.getYearForTurnNo(4));
        assertEquals(0, calendar.getNumberOfPlayedTurns());
        assertEquals(1630, calendar.getCurrentYear());
    }

    @Test
    void negativeRoundDataAndQueriesAreRejectedWithoutMutation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Calendar(1490, 1850, -1));
        final Calendar calendar = new Calendar(1490, 1850, 110);
        assertThrows(IllegalArgumentException.class,
                () -> calendar.getYearForTurnNo(-1));
        final CalendarPo saved = calendar.save();
        saved.setNumberOfPlayedTurns(-1);
        assertThrows(IllegalArgumentException.class,
                () -> Calendar.make(saved));
        assertEquals(110, calendar.getNumberOfPlayedTurns());
        assertEquals(1600, calendar.getCurrentYear());
    }

    @Test
    void seasonalEndBoundaryRejectsAnotherRoundWithoutMutation() {
        final Calendar calendar = new Calendar(1799, 1800, 3);
        assertFalse(calendar.isFinished());
        calendar.endRound();
        assertEquals(1800, calendar.getCurrentYear());
        assertEquals(Calendar.Season.spring, calendar.getCurrentSeason().get());
        assertTrue(calendar.isFinished());
        assertThrows(IllegalStateException.class, calendar::endRound);
        assertEquals(4, calendar.getNumberOfPlayedTurns());
        assertEquals(1800, calendar.getCurrentYear());
    }

    @Test
    public void testEndRoundPositive() {
        final Calendar calendar = createCalendar(1590, 1593);

        calendar.endRound();
        assertEquals(1590, calendar.getStartYear());
        assertEquals(1593, calendar.getEndYear());
        assertEquals(1591, calendar.getCurrentYear());
        assertFalse(calendar.isFinished());

        calendar.endRound();
        assertEquals(1590, calendar.getStartYear());
        assertEquals(1593, calendar.getEndYear());
        assertEquals(1592, calendar.getCurrentYear());
        assertFalse(calendar.isFinished());

        calendar.endRound();
        assertEquals(1590, calendar.getStartYear());
        assertEquals(1593, calendar.getEndYear());
        assertEquals(1593, calendar.getCurrentYear());
        assertTrue(calendar.isFinished());
    }

    @Test
    public void testEndRoundNegative() {
        final Calendar calendar = createCalendar(-1593, -1590);

        calendar.endRound();
        assertEquals(-1593, calendar.getStartYear());
        assertEquals(-1590, calendar.getEndYear());
        assertEquals(-1592, calendar.getCurrentYear());
        assertFalse(calendar.isFinished());

        calendar.endRound();
        assertEquals(-1593, calendar.getStartYear());
        assertEquals(-1590, calendar.getEndYear());
        assertEquals(-1591, calendar.getCurrentYear());
        assertFalse(calendar.isFinished());

        calendar.endRound();
        assertEquals(-1593, calendar.getStartYear());
        assertEquals(-1590, calendar.getEndYear());
        assertEquals(-1590, calendar.getCurrentYear());
        assertTrue(calendar.isFinished());
    }

    @Test()
    public void testEndRoundException() {
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> {
                    final Calendar calendar = createCalendar(1590, 1591);

                    calendar.endRound();
                    calendar.endRound();
                });

        assertEquals("End year (1591) already reached.",
                exception.getMessage());
    }

    private Calendar createCalendar(final int startYear, final int endYear) {
        return new Calendar(startYear, endYear, 0);
    }

}
