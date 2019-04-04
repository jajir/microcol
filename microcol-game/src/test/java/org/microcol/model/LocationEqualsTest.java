package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationEqualsTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(arguments(Location.of(0, 0), Location.of(0, 0)),
                arguments(Location.of(1, 1), Location.of(1, 1)),
                arguments(Location.of(3, 2), Location.of(3, 2)),
                arguments(Location.of(-3, 2), Location.of(-3, 2)),
                arguments(Location.of(3, -2), Location.of(3, -2)),
                arguments(Location.of(-3, -2), Location.of(-3, -2)));
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, location = {1}")
    @MethodSource("dataProvider")

    public void testEquals(final Location location1, final Location location2) {
        assertTrue(location1.equals(location2), "Test of location1 and location2 failed.");
        assertTrue(location2.equals(location1), "Test of location2 and location1 failed.");
    }
}
