package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verify direction enumeration definition.
 */
public class DirectionTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(Location.of(0, -1), true, false, false, false, Direction.north),
                arguments(Location.of(1, -1), true, true, false, false, Direction.northEast),
                arguments(Location.of(1, 0), false, true, false, false, Direction.east),
                arguments(Location.of(1, 1), false, true, true, false, Direction.southEast),
                arguments(Location.of(0, 1), false, false, true, false, Direction.south),
                arguments(Location.of(-1, 1), false, false, true, true, Direction.southWest),
                arguments(Location.of(-1, 0), false, false, false, true, Direction.west),
                arguments(Location.of(-1, -1), true, false, false, true, Direction.northWest));
    }

    @ParameterizedTest(name = "{index}: vector = {0}, isNorth = {1}, isEast = {2}, isSouth = {3}, isWest = {4}, direction = {5}")
    @MethodSource("dataProvider")
    public void test_diOrection(final Location location, final boolean isNorth,
            final boolean isEast, final boolean isSouth, final boolean isWest,
            final Direction direction) {
        Direction found = Direction.valueOf(location);
        assertNotNull(found);
        assertEquals(direction, found);
        assertEquals(isNorth, found.isOrientedNorth());
        assertEquals(isEast, found.isOrientedEast());
        assertEquals(isSouth, found.isOrientedSouth());
        assertEquals(isWest, found.isOrientedWest());
    }

}
