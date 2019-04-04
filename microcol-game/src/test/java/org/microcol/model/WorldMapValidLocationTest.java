package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WorldMapValidLocationTest  extends AbstractMapTest{

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments("/maps/test-map-ocean-10x10.json", Location.of(1, 1)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(10, 1)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(10, 10)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(1, 10)),
                arguments("/maps/test-map-ocean-10x10.json", Location.of(5, 5)));
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, location = {1}")
    @MethodSource("dataProvider")
    public void testValidLocation(final String fileName, final Location location) {
        final WorldMap map = loadPredefinedWorldMap(fileName);

        assertTrue(map.isValid(location));
    }
}
