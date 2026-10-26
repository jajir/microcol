package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import javafx.scene.image.Image;

class TerrainCoastColourTest {
    @Test
    void a_mixed_coast_preserves_mask_and_matches_each_adjacent_land_colour() {
        final ImageCache cache = new ImageCache(new StreamReader());
        final ImageProvider images = new ImageProvider(cache,
                new ImageLoaderImpl(cache));
        final WorldMap world = mock(WorldMap.class);
        when(world.getMaxLocationX()).thenReturn(3);
        when(world.getMaxLocationY()).thenReturn(3);
        when(world.getTerrainTypeAt(any())).thenAnswer(call -> {
            final Location at = call.getArgument(0);
            return at.getY() == 1 ? TerrainType.ARCTIC
                    : at.getY() == 3 ? TerrainType.DESERT : TerrainType.OCEAN;
        });
        when(world.getSeed()).thenReturn(123);
        final Image mask = cache.getImage("grass-well");
        final Image coast = new TerrainBoundaryImages(world, images, at -> true)
                .getCoast(Location.of(2, 2), mask);
        assertEquals(0xf0ede1,
                coast.getPixelReader().getArgb(22, 0) & 0xffffff);
        assertEquals(0xe0c28c,
                coast.getPixelReader().getArgb(22, 44) & 0xffffff);
        for (int x = 0; x < 45; x++) {
            for (int y = 0; y < 45; y++) {
                assertEquals(mask.getPixelReader().getColor(x, y).getOpacity(),
                        coast.getPixelReader().getColor(x, y).getOpacity());
            }
        }
    }
}
