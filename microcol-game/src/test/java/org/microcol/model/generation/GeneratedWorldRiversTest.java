package org.microcol.model.generation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;

class GeneratedWorldRiversTest {

    @Test
    void uplandsAloneCannotCarryARiver() {
        for (final TerrainType type : new TerrainType[] { TerrainType.HILL,
                TerrainType.MOUNTAIN }) {
            final Map<Location, TerrainType> terrain = new HashMap<>();
            for (int x = 5; x <= 25; x++) {
                for (int y = 5; y <= 20; y++) {
                    terrain.put(Location.of(x, y), type);
                }
            }
            assertThrows(IllegalStateException.class,
                    () -> GeneratedWorldRivers.trace(new GeneratedGrid(terrain),
                            new LatticeNoise(new Random(1)), new Random(1)));
        }
    }

    @Test
    void narrowCoastalLandCannotCarryARiver() {
        final Map<Location, TerrainType> terrain = new HashMap<>();
        for (int x = 5; x <= 25; x++) {
            terrain.put(Location.of(x, 10), TerrainType.PLAINS);
        }
        terrain.put(Location.of(5, 10), TerrainType.HILL);
        assertThrows(IllegalStateException.class,
                () -> GeneratedWorldRivers.trace(new GeneratedGrid(terrain),
                        new LatticeNoise(new Random(1)), new Random(1)));
    }
}
