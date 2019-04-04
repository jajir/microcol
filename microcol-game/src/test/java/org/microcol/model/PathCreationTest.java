package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PathCreationTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(

                arguments(Arrays.asList(Location.of(2, 3))),
                arguments(Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4),
                        Location.of(3, 5), Location.of(4, 5))),
                // circle
                arguments(Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4),
                        Location.of(2, 4), Location.of(2, 3))),
                // back
                arguments(Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(4, 3),
                        Location.of(3, 3), Location.of(2, 3))));

    }

    @ParameterizedTest(name = "{index}: locations = {0}")
    @MethodSource("dataProvider")
    public void testCreation(final List<Location> locations) {
        final Path path = Path.of(locations);

        assertEquals(locations, path.getLocations(), "Test of locations failed:");
        assertEquals(locations.get(0), path.getStart(), "Test of start failed:");
    }
}
