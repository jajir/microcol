package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WorldMapInvalidLocationTest extends AbstractMapTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(arguments("/maps/test-map-ocean-10x10.json", Location.of(0, 0)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(0, 5)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(5, 0)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(11, 5)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(5, 11)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(11, 11)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(-1, -1)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(-5, -5)));
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, location = {1}")
    @MethodSource("dataProvider")
    public void testInvalidLocation(final String fileName, final Location location) {
        final WorldMap map = loadPredefinedWorldMap(fileName);

        assertFalse(map.isValid(location));
    }
}
