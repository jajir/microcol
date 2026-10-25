package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

class TerrainDecorationTest {
    private static final ImageCache CACHE = new ImageCache(new StreamReader());
    private static final ImageProvider ASSETS = new ImageProvider(CACHE,
            new ImageLoaderImpl(CACHE));

    private WorldMap world(final int seed) {
        final WorldMap world = mock(WorldMap.class);
        when(world.getSeed()).thenReturn(seed);
        when(world.getMaxLocationX()).thenReturn(100);
        when(world.getMaxLocationY()).thenReturn(100);
        return world;
    }

    @Test
    void agreed_frequencies_and_attribute_priority_cover_every_terrain() {
        final int[] percentages = { 10, 10, 20, 10, 25, 25, 15, 20, 15, 100,
                100, 5, 5, 5 };
        final ImageRandomProvider images = new ImageRandomProvider(ASSETS,
                world(134));
        for (int i = 0; i < TerrainType.TERRAINS.size(); i++) {
            final TerrainType type = TerrainType.TERRAINS.get(i);
            assertEquals(percentages[i],
                    ImageRandomProvider.decorationPercent(type));
            int decorated = 0;
            for (int x = 1; x <= 100; x++) {
                for (int y = 1; y <= 100; y++) {
                    final Location at = Location.of(x, y);
                    if (images.isTerrainDecorated(type, at, false, 0))
                        decorated++;
                    assertEquals(percentages[i] == 100,
                            images.isTerrainDecorated(type, at, true, 0),
                            type.name());
                }
            }
            assertEquals(percentages[i], decorated / 100.0, 1.0, type.name());
        }
    }

    @Test
    void land_is_stable_after_reload_and_water_changes_at_staggered_intervals() {
        final ImageProvider assets = ASSETS;
        final ImageRandomProvider first = new ImageRandomProvider(assets,
                world(134));
        final ImageRandomProvider reloaded = new ImageRandomProvider(assets,
                world(134));
        final ImageRandomProvider different = new ImageRandomProvider(assets,
                world(135));
        int differentSeed = 0;
        int changedWater = 0;
        int nextFrameChanges = 0;
        int waterCount = 0;
        for (int x = 1; x <= 100; x++) {
            for (int y = 1; y <= 100; y++) {
                final Location at = Location.of(x, y);
                final boolean land = first
                        .isTerrainDecorated(TerrainType.PRAIRIE, at, false, 0);
                assertEquals(land, reloaded.isTerrainDecorated(
                        TerrainType.PRAIRIE, at, false, 9000));
                if (land != different.isTerrainDecorated(TerrainType.PRAIRIE,
                        at, false, 0))
                    differentSeed++;
                final boolean water = first
                        .isTerrainDecorated(TerrainType.OCEAN, at, false, 0);
                if (water != first.isTerrainDecorated(TerrainType.OCEAN, at,
                        false, 120))
                    changedWater++;
                if (water != first.isTerrainDecorated(TerrainType.OCEAN, at,
                        false, 1))
                    nextFrameChanges++;
                if (first.isTerrainDecorated(TerrainType.OCEAN, at, false, 120))
                    waterCount++;
            }
        }
        assertTrue(differentSeed > 500,
                "Different worlds must not repeat the same decoration layout");
        assertTrue(changedWater > 500, "Water must move between squares");
        assertTrue(nextFrameChanges > 0 && nextFrameChanges < 50,
                "Water must not switch all at once");
        assertEquals(5, waterCount / 100.0, 1.0);
    }
}
