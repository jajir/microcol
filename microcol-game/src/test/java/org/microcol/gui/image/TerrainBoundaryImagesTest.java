package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.microcol.gui.Tile;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import javafx.scene.image.Image;

class TerrainBoundaryImagesTest {
    private static final int SIZE = Tile.TILE_WIDTH_IN_PX;
    private static final ImageCache CACHE = new ImageCache(new StreamReader());
    private static final ImageProvider IMAGES = new ImageProvider(CACHE,
            new ImageLoaderImpl(CACHE));

    private WorldMap world(final int seed,
            final Function<Location, TerrainType> terrain) {
        final WorldMap world = mock(WorldMap.class);
        when(world.getSeed()).thenReturn(seed);
        when(world.getMaxLocationX()).thenReturn(7);
        when(world.getMaxLocationY()).thenReturn(7);
        when(world.getTerrainTypeAt(any()))
                .thenAnswer(call -> terrain.apply(call.getArgument(0)));
        return world;
    }

    @Test
    void shared_edges_tile_the_map_and_preserve_centres_and_narrow_patches() {
        final List<Function<Location, TerrainType>> layouts = Arrays.asList(
                p -> p.getX() == 4 ? TerrainType.DESERT : TerrainType.PLAINS,
                p -> p.equals(Location.of(4, 4)) ? TerrainType.DESERT
                        : TerrainType.PLAINS,
                p -> p.getX() < 4 ? TerrainType.GRASSLAND
                        : p.getY() < 4 ? TerrainType.PLAINS
                                : TerrainType.DESERT,
                p -> (p.getX() + p.getY()) % 2 == 0 ? TerrainType.DESERT
                        : TerrainType.PLAINS);
        for (final Function<Location, TerrainType> layout : layouts) {
            for (int seed = 11; seed < 15; seed++) {
                final TerrainBoundaryImages renderer = new TerrainBoundaryImages(
                        world(seed, layout), IMAGES, p -> true);
                final Area union = new Area();
                for (int y = 1; y <= 7; y++) {
                    for (int x = 1; x <= 7; x++) {
                        final Path2D shape = renderer
                                .outline(Location.of(x, y));
                        assertTrue(shape.contains((x - 0.5) * SIZE,
                                (y - 0.5) * SIZE));
                        final Area overlap = new Area(shape);
                        overlap.intersect(union);
                        assertTrue(overlap.isEmpty(),
                                "Neighbouring regions must not overlap");
                        union.add(new Area(shape));
                        final List<double[]> edges = edges(shape);
                        for (int edge = 0; edge < 4; edge++) {
                            final double axis = edge % 2 == 0
                                    ? (y - 1 + edge / 2) * SIZE
                                    : (x - (edge == 3 ? 1 : 0)) * SIZE;
                            for (int point = 0; point < 4; point++) {
                                final double coordinate = edges
                                        .get(edge)[point * 2
                                                + (edge % 2 == 0 ? 1 : 0)];
                                assertTrue(
                                        Math.abs(coordinate - axis) <= SIZE
                                                * 0.18 + 1e-8,
                                        "Every curve stays in the bounded edge band");
                            }
                        }
                        if (x < 7) {
                            final double[] right = edges.get(1);
                            final double[] left = edges(
                                    renderer.outline(Location.of(x + 1, y)))
                                    .get(3);
                            for (int point = 0; point < 4; point++) {
                                assertEquals(right[point * 2],
                                        left[(3 - point) * 2]);
                                assertEquals(right[point * 2 + 1],
                                        left[(3 - point) * 2 + 1]);
                            }
                        }
                    }
                }
                union.exclusiveOr(new Area(
                        new Rectangle2D.Double(0, 0, 7 * SIZE, 7 * SIZE)));
                assertTrue(union.isEmpty(),
                        "There must be no holes, including at multi-terrain junctions");
            }
        }
    }

    @Test
    void pixels_are_cached_stable_on_reload_and_change_with_seed() {
        final Function<Location, TerrainType> layout = p -> p.getX() < 4
                ? TerrainType.GRASSLAND
                : TerrainType.DESERT;
        final WorldMap map = world(42, layout);
        final TerrainBoundaryImages first = new TerrainBoundaryImages(map,
                IMAGES, p -> true);
        final Location at = Location.of(3, 3);
        final Image image = first.getBackground(at, TerrainType.GRASSLAND);
        assertSame(image, first.getBackground(at, TerrainType.GRASSLAND));
        assertArrayEquals(pixels(image),
                pixels(new TerrainBoundaryImages(map, IMAGES, p -> true)
                        .getBackground(at, TerrainType.GRASSLAND)));
        assertFalse(Arrays.equals(pixels(image), pixels(
                new TerrainBoundaryImages(world(43, layout), IMAGES, p -> true)
                        .getBackground(at, TerrainType.GRASSLAND))));
        assertEquals(0xffa9b77a, image.getPixelReader().getArgb(22, 22));
        assertNull(
                first.getBackground(Location.of(1, 3), TerrainType.GRASSLAND));
        assertNull(first.getBackground(at, TerrainType.OCEAN));
        for (final int argb : pixels(image)) {
            assertEquals(255, argb >>> 24);
        }
    }

    @Test
    void unknown_terrain_never_influences_visible_pixels_and_exploration_invalidates_cache() {
        final AtomicReference<TerrainType> hidden = new AtomicReference<>(
                TerrainType.DESERT);
        final AtomicBoolean revealed = new AtomicBoolean(false);
        final WorldMap map = world(21,
                p -> p.getX() < 4 ? TerrainType.GRASSLAND : hidden.get());
        final TerrainBoundaryImages renderer = new TerrainBoundaryImages(map,
                IMAGES, p -> p.getX() < 4 || revealed.get());
        final Location at = Location.of(3, 3);
        assertNull(renderer.getBackground(at, TerrainType.GRASSLAND));
        hidden.set(TerrainType.MARSH);
        assertNull(renderer.getBackground(at, TerrainType.GRASSLAND));
        final double[] before = edges(renderer.outline(at)).get(1);
        revealed.set(true);
        final Image image = renderer.getBackground(at, TerrainType.GRASSLAND);
        assertNotNull(image);
        assertFalse(Arrays.equals(before, edges(renderer.outline(at)).get(1)));
        assertSame(image, renderer.getBackground(at, TerrainType.GRASSLAND));
        verify(map, never()).getTerrainTypeAt(Location.of(0, 3));
    }

    @Test
    void coastal_colours_use_only_explored_land_and_keep_the_mask() {
        final AtomicReference<TerrainType> hidden = new AtomicReference<>(
                TerrainType.DESERT);
        final WorldMap map = world(9, p -> p.getY() == 3 ? TerrainType.OCEAN
                : p.getY() < 3 ? TerrainType.ARCTIC : hidden.get());
        final TerrainBoundaryImages renderer = new TerrainBoundaryImages(map,
                IMAGES, p -> p.getY() < 4);
        final Image mask = CACHE.getImage("grass-well");
        final Image image = renderer.getCoast(Location.of(3, 3), mask);
        hidden.set(TerrainType.MARSH);
        assertSame(image, renderer.getCoast(Location.of(3, 3), mask));
        assertEquals(0xf0ede1,
                image.getPixelReader().getArgb(22, 0) & 0xffffff);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                assertEquals(mask.getPixelReader().getColor(x, y).getOpacity(),
                        image.getPixelReader().getColor(x, y).getOpacity());
            }
        }
    }

    private int[] pixels(final Image image) {
        final int[] pixels = new int[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y * SIZE + x] = image.getPixelReader().getArgb(x, y);
            }
        }
        return pixels;
    }

    private List<double[]> edges(final Path2D path) {
        final List<double[]> result = new ArrayList<>();
        final PathIterator iterator = path.getPathIterator(null);
        final double[] p = new double[6];
        double x = 0, y = 0;
        while (!iterator.isDone()) {
            final int kind = iterator.currentSegment(p);
            if (kind == PathIterator.SEG_CUBICTO) {
                result.add(new double[] { x, y, p[0], p[1], p[2], p[3], p[4],
                        p[5] });
                x = p[4];
                y = p[5];
            } else if (kind == PathIterator.SEG_MOVETO) {
                x = p[0];
                y = p[1];
            }
            iterator.next();
        }
        return result;
    }
}
