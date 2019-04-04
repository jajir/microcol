package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationCreationTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(0, 0),
                arguments(1, 1),
                arguments(2, 3),
                arguments(-2, 3),
                arguments(2, -3),
                arguments(-2, -3)
            );
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, location = {1}")
    @MethodSource("dataProvider")
    public void testCreation(final int x, final int y) {
        final Location location = Location.of(x, y);

        assertEquals(x, location.getX(), "Test of X-axis failed:");
        assertEquals(y, location.getY(), "Test of Y-axis failed:");
    }
}
