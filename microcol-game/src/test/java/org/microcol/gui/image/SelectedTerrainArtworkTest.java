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
    void lake_frames_expand_three_transparent_rings_then_rest() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrainSymbols().preload(cache);
        final Set<Long> frames = new HashSet<>();
        for (int frame = 0; frame < 120; frame++) {
            final Image image = cache.getImage(
                    ImageLoaderTerrainSymbols.waveKey(TerrainType.LAKE, frame));
            assertEquals(45, image.getWidth());
            assertEquals(45, image.getHeight());
            frames.add(fingerprint(image));
            for (int y = 0; y < 45; y++) {
                for (int x = 0; x < 45; x++) {
                    final int pixel = image.getPixelReader().getArgb(x, y);
                    if ((pixel >>> 24) > 0) {
                        assertEquals(0, pixel & 0x00ffffff,
                                "Lake motion is transparent black ink, never a filled lake");
                        assertTrue(x >= 8 && x <= 42 && y >= 9 && y <= 29,
                                "Ripples remain inside their tile");
                    }
                }
            }
            if (frame == 0 || frame >= 112) {
                assertEquals(0, ink(image),
                        "The cycle starts and ends at rest");
            }
        }
        assertTrue(frames.size() > 100, "Ripples expand and fade smoothly");
        final Image threeRings = cache.getImage(
                ImageLoaderTerrainSymbols.waveKey(TerrainType.LAKE, 50));
        int crossings = 0;
        boolean previousInk = false;
        for (int x = 0; x < 45; x++) {
            final boolean hasInk = threeRings.getPixelReader().getColor(x, 19)
                    .getOpacity() > 0.1;
            if (hasInk && !previousInk) {
                crossings++;
            }
            previousInk = hasInk;
        }
        assertEquals(6, crossings, "A diameter crosses three open rings twice");
        assertEquals(0,
                threeRings.getPixelReader().getColor(25, 19).getOpacity(),
                "The ring centres remain transparent");
        assertEquals(fingerprint(threeRings),
                fingerprint(cache.getImage(ImageLoaderTerrainSymbols
                        .decorationKey(TerrainType.LAKE))),
                "Static previews show the selected three-ring phase");
    }

    @Test
    void wide_wash_frames_fall_spread_and_dissolve_with_a_visible_static_crest() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrainSymbols().preload(cache);
        for (final TerrainType terrain : new TerrainType[] { TerrainType.OCEAN,
                TerrainType.HIGH_SEA }) {
            final Set<Long> frames = new HashSet<>();
            for (int frame = 0; frame < 120; frame++) {
                final Image image = cache.getImage(
                        ImageLoaderTerrainSymbols.waveKey(terrain, frame));
                assertEquals(45, image.getWidth());
                assertEquals(45, image.getHeight());
                frames.add(fingerprint(image));
                for (int y = 0; y < 45; y++) {
                    for (int x = 0; x < 45; x++) {
                        final int pixel = image.getPixelReader().getArgb(x, y);
                        // The pale High Seas direction marker starts at x=37.
                        if (x < 35 && (pixel >>> 24) > 0) {
                            assertEquals(0, pixel & 0x00ffffff,
                                    "Wave contours use black ink");
                        }
                        if (x < 3 || y < 16 || y > 32) {
                            assertEquals(0,
                                    image.getPixelReader().getColor(x, y)
                                            .getOpacity(),
                                    "Waves leave room for units and overlays");
                        }
                    }
                }
            }
            assertTrue(frames.size() > 100, "Smooth motion and fading");
            final Image crest = cache
                    .getImage(ImageLoaderTerrainSymbols.waveKey(terrain, 53));
            final Image impact = cache
                    .getImage(ImageLoaderTerrainSymbols.waveKey(terrain, 59));
            final Image dissolved = cache
                    .getImage(ImageLoaderTerrainSymbols.waveKey(terrain, 98));
            assertNotEquals(fingerprint(crest), fingerprint(impact));
            assertTrue(ink(crest) > ink(dissolved) + 10,
                    "The wave dissolves; the High Seas direction mark stays");
            assertTrue(ink(crest) > 50 && ink(crest) < 100,
                    "Map-outline weight, not a hairline or a filled hill");
            assertEquals(fingerprint(crest),
                    fingerprint(cache.getImage(
                            ImageLoaderTerrainSymbols.decorationKey(terrain))),
                    "Static previews show the recognizable crest");
            for (int x = 0; x < 45; x++) {
                assertEquals(0,
                        dissolved.getPixelReader().getColor(x, 28).getOpacity(),
                        "The wash fully disappears");
            }
        }
    }

    private double ink(final Image image) {
        double ink = 0;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                ink += image.getPixelReader().getColor(x, y).getOpacity();
            }
        }
        return ink;
    }

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
