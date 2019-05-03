package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.WorldMapPo;

public class WorldMapCreationTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(arguments(15, 10, 128), arguments(34, 29, 256));
    }

    @ParameterizedTest(name = "{index}: maxX = {0}, maxY = {1}, seed = {2}")
    @MethodSource("dataProvider")
    public void testCreation(final int maxX, final int maxY, final Integer seed) {
        WorldMapPo mapPo = new WorldMapPo();
        mapPo.setMaxX(maxX);
        mapPo.setMaxY(maxY);
        mapPo.setSeed(seed);
        mapPo.setTerrainType(new HashMap<>());
        ModelPo gamePo = new ModelPo();
        gamePo.setMap(mapPo);
        final WorldMap map = new WorldMap(gamePo);

        assertEquals(maxX, map.getMaxLocationX(), "Test of maxX failed.");
        assertEquals(maxY, map.getMaxLocationY(), "Test of maxY failed.");
        assertEquals(seed, map.getSeed(), "Test of seed failed.");
    }
}
