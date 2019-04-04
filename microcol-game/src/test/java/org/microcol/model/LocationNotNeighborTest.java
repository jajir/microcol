package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationNotNeighborTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(

                // [0, 0]
                arguments(Location.of(0, 0), Location.of(0, 0)),
                arguments(Location.of(0, 0), Location.of(-2, -1)),
                arguments(Location.of(0, 0), Location.of(-1, -2)),
                arguments(Location.of(0, 0), Location.of(1, -2)),
                arguments(Location.of(0, 0), Location.of(2, -1)),
                arguments(Location.of(0, 0), Location.of(2, 1)),
                arguments(Location.of(0, 0), Location.of(1, 2)),
                arguments(Location.of(0, 0), Location.of(-1, 2)),
                arguments(Location.of(0, 0), Location.of(-2, 1)),
                // [1, 1]
                arguments(Location.of(1, 1), Location.of(1, 1)),
                arguments(Location.of(1, 1), Location.of(-1, 0)),
                arguments(Location.of(1, 1), Location.of(0, -1)),
                arguments(Location.of(1, 1), Location.of(2, -1)),
                arguments(Location.of(1, 1), Location.of(3, 0)),
                arguments(Location.of(1, 1), Location.of(3, 2)),
                arguments(Location.of(1, 1), Location.of(2, 3)),
                arguments(Location.of(1, 1), Location.of(0, 3)),
                arguments(Location.of(1, 1), Location.of(-1, 2)),
                // [5, 5]
                arguments(Location.of(5, 5), Location.of(5, 5)),
                arguments(Location.of(5, 5), Location.of(3, 4)),
                arguments(Location.of(5, 5), Location.of(4, 3)),
                arguments(Location.of(5, 5), Location.of(6, 3)),
                arguments(Location.of(5, 5), Location.of(7, 4)),
                arguments(Location.of(5, 5), Location.of(7, 6)),
                arguments(Location.of(5, 5), Location.of(6, 7)),
                arguments(Location.of(5, 5), Location.of(4, 7)),
                arguments(Location.of(5, 5), Location.of(3, 6)),
                // [-5, -5]
                arguments(Location.of(-5, -5), Location.of(-5, -5)),
                arguments(Location.of(-5, -5), Location.of(-3, -4)),
                arguments(Location.of(-5, -5), Location.of(-4, -3)),
                arguments(Location.of(-5, -5), Location.of(-6, -3)),
                arguments(Location.of(-5, -5), Location.of(-7, -4)),
                arguments(Location.of(-5, -5), Location.of(-7, -6)),
                arguments(Location.of(-5, -5), Location.of(-6, -7)),
                arguments(Location.of(-5, -5), Location.of(-4, -7)),
                arguments(Location.of(-5, -5), Location.of(-3, -6)));

    }

    @ParameterizedTest(name = "{index}: location1 = {0}, location2 = {1}")
    @MethodSource("dataProvider")
    public void testNotNeighbor(final Location location1, final Location location2) {
        assertFalse(location1.isNeighbor(location2), "Test of location1 and location2 failed.");
        assertFalse(location2.isNeighbor(location1), "Test of location2 and location1 failed.");
    }
}
