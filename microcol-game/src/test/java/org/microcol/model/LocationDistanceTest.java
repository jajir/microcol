package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationDistanceTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(

                // [0, 0]
                arguments(Location.of(0, 0), Location.of(0, 0), 0),
                // [1, 1]
                arguments(Location.of(1, 1), Location.of(1, 1), 0),
                // [3, 2]
                arguments(Location.of(3, 2), Location.of(3, 2), 0),
                arguments(Location.of(3, 2), Location.of(-3, 2), 6),
                arguments(Location.of(3, 2), Location.of(3, -2), 4),
                arguments(Location.of(3, 2), Location.of(-3, -2), 7),
                // [-3, 2]
                arguments(Location.of(-3, 2), Location.of(3, 2), 6),
                arguments(Location.of(-3, 2), Location.of(-3, 2), 0),
                arguments(Location.of(-3, 2), Location.of(3, -2), 7),
                arguments(Location.of(-3, 2), Location.of(-3, -2), 4),
                // [3, -2]
                arguments(Location.of(3, -2), Location.of(3, 2), 4),
                arguments(Location.of(3, -2), Location.of(-3, 2), 7),
                arguments(Location.of(3, -2), Location.of(3, -2), 0),
                arguments(Location.of(3, -2), Location.of(-3, -2), 6),
                // [-3, -2]
                arguments(Location.of(-3, -2), Location.of(3, 2), 7),
                arguments(Location.of(-3, -2), Location.of(-3, 2), 4),
                arguments(Location.of(-3, -2), Location.of(3, -2), 6),
                arguments(Location.of(-3, -2), Location.of(-3, -2), 0));

    }

    @ParameterizedTest(name = "{index}: location1 = {0}, location2 = {1}, expected = {2}")
    @MethodSource("dataProvider")
    public void testDistance(final Location location1, final Location location2,
            final int expected) {
        assertEquals(expected, location1.getDistance(location2),
                "Test of location1 and location2 failed.");
        assertEquals(expected, location2.getDistance(location1),
                "Test of location2 and location1 failed.");
    }
}
