package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationNotEqualsTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(Location.of(2, 3), null),
                arguments(Location.of(2, 3), 10),
                arguments(Location.of(2, 3), "test"),
                arguments(Location.of(2, 3), Location.of(3, 2)),
                arguments(Location.of(2, 3), Location.of(-2, 3)),
                arguments(Location.of(2, 3), Location.of(2, -3)),
                arguments(Location.of(2, 3), Location.of(-2, -3)));
    }

    @ParameterizedTest(name = "{index}: location = {0}, object = {1}")
    @MethodSource("dataProvider")
    public void testNotEquals(final Location location, final Object object) {
        assertFalse(location.equals(object));
    }
}
