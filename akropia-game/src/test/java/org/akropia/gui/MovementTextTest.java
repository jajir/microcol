package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class MovementTextTest {
    @ParameterizedTest
    @CsvSource({ "0,0", "1,⅓", "2,⅔", "3,1", "4,1 ⅓", "5,1 ⅔", "6,2",
            "16,5 ⅓" })
    void exactThirds(final int stored, final String shown) {
        assertEquals(shown, MovementText.format(stored));
    }

    @Test
    void negativeMovementIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MovementText.format(-1));
    }
}
