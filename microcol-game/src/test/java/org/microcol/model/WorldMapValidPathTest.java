package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.store.ModelDao;

public class WorldMapValidPathTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments("/maps/test-map-ocean-10x10.json",
                        Arrays.asList(Location.of(1, 1), Location.of(2, 2), Location.of(3, 3))),
                arguments("/maps/test-map-ocean-10x10.json",
                        Arrays.asList(Location.of(10, 10), Location.of(9, 9), Location.of(8, 8))),
                arguments("/maps/test-map-ocean-10x10.json",
                        Arrays.asList(Location.of(4, 6), Location.of(5, 5), Location.of(6, 4))));
    }

    @ParameterizedTest(name = "{index}: fileName = {0}, locations = {1}")
    @MethodSource("dataProvider")
    public void testValidPath(final String fileName, final List<Location> locations) {
        final ModelDao dao = new ModelDao();
        final WorldMap map = dao.loadPredefinedWorldMap(fileName);
        final Path path = Path.of(locations);

        assertTrue(map.isValid(path));
    }
}
