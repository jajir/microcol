package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CalendarTest {

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
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            final Calendar calendar = createCalendar(1590, 1591);

            calendar.endRound();
            calendar.endRound();
        });

        assertEquals("End year (1591) already reached.", exception.getMessage());
    }

    private Calendar createCalendar(final int startYear, final int endYear) {
        return new Calendar(startYear, endYear, 0);
    }

}
