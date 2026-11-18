package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalendarSeasonTest {
    @ParameterizedTest
    @CsvSource({ "0,summer", "20,summer", "21,autumn", "41,autumn", "42,winter",
            "62,winter", "63,spring", "83,spring", "84,summer" })
    void seasonBoundaries(int day, Calendar.Season expected) {
        assertEquals(expected,
                new Calendar(1, 20, day).getCurrentSeason().orElseThrow());
    }
}
