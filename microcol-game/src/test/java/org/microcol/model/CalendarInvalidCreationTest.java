package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalendarInvalidCreationTest {

    @ParameterizedTest(name = "{index}: startYear = {0}, endYear = {1}")
    @CsvSource({ "1750, 1590", "1590, 1590", "-1590, -1750", "-1590, -1590" })
    public void testInvalidCreation(final int startYear, final int endYear) {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    new Calendar(startYear, startYear, 0);
                });

        // verify that exception contains expected message
        assertTrue(exception.getMessage().contains("must be less than end year"));
    }
}
