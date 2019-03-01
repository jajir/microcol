package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationNeighborsTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(Location.of(0, 0),
                        Direction.getVectors()),
                arguments(Location.of(1, 1),
                        Arrays.asList(
                                Location.of(1, 0),
                                Location.of(2, 0),
                                Location.of(2, 1),
                                Location.of(2, 2),
                                Location.of(1, 2),
                                Location.of(0, 2),
                                Location.of(0, 1),
                                Location.of(0, 0))),
                arguments(Location.of(3, 2),
                        Arrays.asList(
                                Location.of(3, 1),
                                Location.of(4, 1),
                                Location.of(4, 2),
                                Location.of(4, 3),
                                Location.of(3, 3),
                                Location.of(2, 3),
                                Location.of(2, 2),
                                Location.of(2, 1))),
                arguments(Location.of(-3, 2),
                        Arrays.asList(
                                Location.of(-3, 1),
                                Location.of(-2, 1),
                                Location.of(-2, 2),
                                Location.of(-2, 3),
                                Location.of(-3, 3),
                                Location.of(-4, 3),
                                Location.of(-4, 2),
                                Location.of(-4, 1))),
                arguments(Location.of(3, -2),
                        Arrays.asList(
                                Location.of(3, -3),
                                Location.of(4, -3),
                                Location.of(4, -2),
                                Location.of(4, -1),
                                Location.of(3, -1),
                                Location.of(2, -1),
                                Location.of(2, -2),
                                Location.of(2, -3))),
                arguments(Location.of(-3, -2),
                        Arrays.asList(
                                Location.of(-3, -3),
                                Location.of(-2, -3),
                                Location.of(-2, -2),
                                Location.of(-2, -1),
                                Location.of(-3, -1),
                                Location.of(-4, -1),
                                Location.of(-4, -2),
                                Location.of(-4, -3))));

    }

    @ParameterizedTest(name = "{index}: {0}, neighbors = {1}")
    @MethodSource("dataProvider")
    public void testNeighbors(final Location location, final List<Location> neighbors) {
        assertEquals(neighbors, location.getNeighbors());
    }
}
