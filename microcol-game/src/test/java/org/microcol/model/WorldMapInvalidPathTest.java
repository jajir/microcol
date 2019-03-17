package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WorldMapInvalidPathTest  extends AbstractMapTest{
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {});
    }

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments("/maps/test-map-ocean-10x10.json",
                        Arrays.asList(Location.of(2, 2), Location.of(1, 1), Location.of(0, 0))),
                arguments("/maps/test-map-ocean-10x10.json", Arrays.asList(Location.of(9, 9),
                        Location.of(10, 10), Location.of(11, 11))));
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, locations = {1}")
    @MethodSource("dataProvider")
    public void testInalidPath(final String fileName, List<Location> locations) {
        WorldMap map = loadPredefinedWorldMap(fileName);

        final Path path = Path.of(locations);

        assertFalse(map.isValid(path));
    }
}
