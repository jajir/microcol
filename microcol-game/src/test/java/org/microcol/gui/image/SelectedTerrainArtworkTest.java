package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;
import javafx.scene.image.Image;

class SelectedTerrainArtworkTest {

    @Test
    void all_selected_terrain_and_resource_images_are_distinct_and_tile_sized() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrainSymbols().preload(cache);
        final TerrainImageProvider terrainImages = new TerrainImageProvider(
                cache);
        final Set<Long> fingerprints = new HashSet<>();
        final Set<Integer> colours = new HashSet<>();
        for (final TerrainType terrain : TerrainType.TERRAINS) {
            final Image image = terrainImages.getTerrainImage(terrain);
            assertEquals(45, image.getWidth());
            assertEquals(45, image.getHeight());
            assertEquals(1, image.getPixelReader().getColor(0, 0).getOpacity());
            assertTrue(fingerprints.add(fingerprint(image)), terrain.name());
            assertTrue(colours.add(image.getPixelReader().getArgb(0, 0)),
                    terrain.name());
            final Image decoration = cache
                    .getImage(ImageLoaderTerrainSymbols.decorationKey(terrain));
            assertEquals(0,
                    decoration.getPixelReader().getColor(0, 0).getOpacity());
            int ink = 0;
            for (int y = 0; y < 45; y++) {
                for (int x = 0; x < 45; x++) {
                    if (decoration.getPixelReader().getColor(x, y)
                            .getOpacity() >= 0.5)
                        ink++;
                }
            }
            assertTrue(ink > 20 && ink < 1400, terrain.name());
        }
        for (final ResourceOverlay resource : ResourceOverlay.values()) {
            if (resource == ResourceOverlay.NONE) {
                continue;
            }
            final Image image = cache
                    .getImage(ImageLoaderTerrainSymbols.resourceKey(resource));
            assertEquals(45, image.getWidth());
            assertEquals(45, image.getHeight());
            assertTrue(fingerprints.add(fingerprint(image)), resource.name());
            int inkPixels = 0;
            for (int y = 0; y < 45; y++) {
                for (int x = 0; x < 45; x++) {
                    final double alpha = image.getPixelReader().getColor(x, y)
                            .getOpacity();
                    if (x < 17 || y > 28) {
                        assertEquals(0, alpha,
                                "Transparent space around " + resource);
                    }
                    if (alpha == 1) {
                        inkPixels++;
                    }
                }
            }
            assertTrue(inkPixels > 50,
                    "Readable opaque silhouette for " + resource);
            assertTrue(inkPixels < 700,
                    "No solid background around " + resource);
        }
        assertEquals(26, fingerprints.size());
        assertThrows(IllegalArgumentException.class,
                () -> ImageLoaderTerrainSymbols
                        .resourceKey(ResourceOverlay.NONE));
    }

    private long fingerprint(final Image image) {
        long hash = 1;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                hash = 31 * hash + image.getPixelReader().getArgb(x, y);
            }
        }
        return hash;
    }
}
