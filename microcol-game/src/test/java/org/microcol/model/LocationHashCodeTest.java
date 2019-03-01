package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationHashCodeTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(Location.of(0, 0), 0),
                arguments(Location.of(1, 0), 1),
                arguments(Location.of(-1, 0), -1),
                arguments(Location.of(0, 1), 65536),
                arguments(Location.of(0, -1), -65536),
                arguments(Location.of(1, 1), 65537),
                arguments(Location.of(-1, -1), -65537),
                arguments(Location.of(2, 3), 196610),
                arguments(Location.of(-2, 3), 196606),
                arguments(Location.of(2, -3), -196606),
                arguments(Location.of(-2, -3), -196610),
                arguments(Location.of(3, 2), 131075),
                arguments(Location.of(-3, 2), 131069),
                arguments(Location.of(3, -2), -131069),
                arguments(Location.of(-3, -2), -131075));
    }

    @ParameterizedTest(name = "{index}: location = {0}, expected = {1}")
    @MethodSource("dataProvider")
    public void testHashCode(final Location location, final int expected) {
        assertEquals(expected, location.hashCode());
    }
}
