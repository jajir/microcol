package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationAddTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // [3, 2]
                arguments(Location.of(3, 2), Location.of(3, 2), Location.of(6, 4)),
                arguments(Location.of(3, 2), Location.of(-3, 2), Location.of(0, 4)),
                arguments(Location.of(3, 2), Location.of(3, -2), Location.of(6, 0)),
                arguments(Location.of(3, 2), Location.of(-3, -2), Location.of(0, 0)),
                // [-3, 2]
                arguments(Location.of(-3, 2), Location.of(3, 2), Location.of(0, 4)),
                arguments(Location.of(-3, 2), Location.of(-3, 2), Location.of(-6, 4)),
                arguments(Location.of(-3, 2), Location.of(3, -2), Location.of(0, 0)),
                arguments(Location.of(-3, 2), Location.of(-3, -2), Location.of(-6, 0)),
                // [3, -2]
                arguments(Location.of(3, -2), Location.of(3, 2), Location.of(6, 0)),
                arguments(Location.of(3, -2), Location.of(-3, 2), Location.of(0, 0)),
                arguments(Location.of(3, -2), Location.of(3, -2), Location.of(6, -4)),
                arguments(Location.of(3, -2), Location.of(-3, -2), Location.of(0, -4)),
                // [-3, -2]
                arguments(Location.of(-3, -2), Location.of(3, 2), Location.of(0, 0)),
                arguments(Location.of(-3, -2), Location.of(-3, 2), Location.of(-6, 0)),
                arguments(Location.of(-3, -2), Location.of(3, -2), Location.of(0, -4)),
                arguments(Location.of(-3, -2), Location.of(-3, -2), Location.of(-6, -4)));

    }

    @ParameterizedTest(name = "{index}: fileName = {0}, location = {1}")
    @MethodSource("dataProvider")
    public void testAdd(final Location location1, final Location location2,
            final Location expected) {
        assertEquals(expected, location1.add(location2), "Test of location1 and location2 failed:");
        assertEquals(expected, location2.add(location1), "Test of location2 and location1 failed:");
    }
}
