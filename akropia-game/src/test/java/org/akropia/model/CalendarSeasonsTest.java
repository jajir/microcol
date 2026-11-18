package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalendarSeasonsTest {
    @ParameterizedTest
    @CsvSource({ "6,1,1,7", "7,1,2,1", "20,1,3,7", "21,1,4,1", "83,1,12,7",
            "84,2,1,1", "167,2,12,7", "168,3,1,1" })
    void completedRoundChangesDate(int day, int year, int month, int monthDay) {
        Calendar calendar = new Calendar(1, 20, day - 1);
        calendar.endRound();
        assertEquals(year, calendar.getCurrentYear());
        assertEquals(month, calendar.getMonthOfYear());
        assertEquals(monthDay, calendar.getDayOfMonth());
    }
}
