package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationNeighborTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // [0, 0]
                arguments(Location.of(0, 0), Location.of(-1, -1)),
                arguments(Location.of(0, 0), Location.of(0, -1)),
                arguments(Location.of(0, 0), Location.of(1, -1)),
                arguments(Location.of(0, 0), Location.of(-1, 0)),
                arguments(Location.of(0, 0), Location.of(1, 0)),
                arguments(Location.of(0, 0), Location.of(-1, 1)),
                arguments(Location.of(0, 0), Location.of(0, 1)),
                arguments(Location.of(0, 0), Location.of(1, 1)),
                // [1, 1]
                arguments(Location.of(1, 1), Location.of(0, 0)),
                arguments(Location.of(1, 1), Location.of(1, 0)),
                arguments(Location.of(1, 1), Location.of(2, 0)),
                arguments(Location.of(1, 1), Location.of(0, 1)),
                arguments(Location.of(1, 1), Location.of(2, 1)),
                arguments(Location.of(1, 1), Location.of(0, 2)),
                arguments(Location.of(1, 1), Location.of(1, 2)),
                arguments(Location.of(1, 1), Location.of(2, 2)),
                // [5, 5]
                arguments(Location.of(5, 5), Location.of(4, 4)),
                arguments(Location.of(5, 5), Location.of(5, 4)),
                arguments(Location.of(5, 5), Location.of(6, 4)),
                arguments(Location.of(5, 5), Location.of(4, 5)),
                arguments(Location.of(5, 5), Location.of(6, 5)),
                arguments(Location.of(5, 5), Location.of(4, 6)),
                arguments(Location.of(5, 5), Location.of(5, 6)),
                arguments(Location.of(5, 5), Location.of(6, 6)),
                // [-5, -5]
                arguments(Location.of(-5, -5), Location.of(-4, -4)),
                arguments(Location.of(-5, -5), Location.of(-5, -4)),
                arguments(Location.of(-5, -5), Location.of(-6, -4)),
                arguments(Location.of(-5, -5), Location.of(-4, -5)),
                arguments(Location.of(-5, -5), Location.of(-6, -5)),
                arguments(Location.of(-5, -5), Location.of(-4, -6)),
                arguments(Location.of(-5, -5), Location.of(-5, -6)),
                arguments(Location.of(-5, -5), Location.of(-6, -6)));

    }

    @ParameterizedTest(name = "{index}: location1 = {0}, location2 = {1}")
    @MethodSource("dataProvider")
    public void testNeighbor(final Location location1, final Location location2) {
        assertTrue(location1.isNeighbor(location2), "Test of location1 and location2 failed.");
        assertTrue(location2.isNeighbor(location1), "Test of location2 and location1 failed.");
    }
}
