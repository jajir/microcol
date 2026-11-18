package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CalendarTest {
    @Test
    void daysMonthsSeasonsAndYearsAdvanceTogether() {
        Calendar calendar = new Calendar(1, 20, 0);
        String[] names = { "Hekatombaion", "Metageitnion", "Boedromion",
                "Pyanepsion", "Maimakterion", "Poseideon", "Gamelion",
                "Anthesterion", "Elaphebolion", "Mounichion", "Thargelion",
                "Skirophorion" };
        for (int day = 0; day < 168; day++) {
            assertEquals(1 + day / 84, calendar.getCurrentYear());
            assertEquals(1 + day % 7, calendar.getDayOfMonth());
            assertEquals(1 + day % 84 / 7, calendar.getMonthOfYear());
            assertEquals(names[day % 84 / 7], calendar.getMonthName());
            assertEquals(21 - day % 21, calendar.getDaysUntilNextSeason());
            calendar.endRound();
        }
    }

    @Test
    void historicalQueriesDoNotMutateTheCurrentDate() {
        Calendar calendar = new Calendar(1, 20, 110);
        assertEquals(1, calendar.getYearForTurnNo(0));
        assertEquals(1, calendar.getYearForTurnNo(83));
        assertEquals(2, calendar.getYearForTurnNo(84));
        assertEquals(3, calendar.getYearForTurnNo(168));
        assertEquals(110, calendar.getNumberOfPlayedTurns());
        assertEquals(2, calendar.getCurrentYear());
        assertThrows(IllegalArgumentException.class,
                () -> calendar.getYearForTurnNo(-1));
    }

    @Test
    void invalidSavedTurnsAndMissingLimitAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Calendar(1, 20, -1));
        var saved = new Calendar(1, 20, 0).save();
        saved.setNumberOfPlayedTurns(-1);
        assertThrows(IllegalArgumentException.class,
                () -> Calendar.make(saved));
        saved.setNumberOfPlayedTurns(0);
        saved.setTurnLimit(null);
        assertThrows(IllegalArgumentException.class,
                () -> Calendar.make(saved));
    }

    @Test
    void authoredMissionDeadlineIsInDaysAndDoesNotMutateOnRejection() {
        var saved = new Calendar(1, 20, 6).save();
        saved.setTurnLimit(7);
        Calendar calendar = Calendar.make(saved);
        assertFalse(calendar.isFinished());
        calendar.endRound();
        assertTrue(calendar.isFinished());
        assertThrows(IllegalStateException.class, calendar::endRound);
        assertEquals(7, calendar.getNumberOfPlayedTurns());
    }
}
