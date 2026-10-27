package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import javafx.scene.image.Image;

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
    void unknown_marks_are_sparse_and_fixed_by_the_saved_map_seed() {
        final ImageRandomProvider first = new ImageRandomProvider(ASSETS,
                world(134));
        final ImageRandomProvider reloaded = new ImageRandomProvider(ASSETS,
                world(134));
        final ImageRandomProvider otherMap = new ImageRandomProvider(ASSETS,
                world(135));
        int marks = 0;
        int changed = 0;
        for (int x = 1; x <= 100; x++) {
            for (int y = 1; y <= 100; y++) {
                final Location at = Location.of(x, y);
                final boolean marked = first.hasUnknownMark(at);
                assertEquals(marked, reloaded.hasUnknownMark(at));
                marks += marked ? 1 : 0;
                changed += marked != otherMap.hasUnknownMark(at) ? 1 : 0;
            }
        }
        assertTrue(marks >= 300 && marks <= 500,
                "About 4% of 10,000 squares should carry a mark: " + marks);
        assertTrue(changed > 500, "Different seeds should change placement");
    }

    @Test
    void agreed_frequencies_and_attribute_priority_cover_every_terrain() {
        final int[] percentages = { 10, 10, 20, 10, 25, 25, 15, 20, 15, 100,
                100, 5, 5, 100 };
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
                    assertEquals(
                            type == TerrainType.HILL
                                    || type == TerrainType.MOUNTAIN,
                            images.isTerrainDecorated(type, at, true, 0),
                            type.name());
                }
            }
            assertEquals(percentages[i], decorated / 100.0, 1.0, type.name());
        }
    }

    @Test
    void sea_frames_loop_over_six_seconds_and_keep_seeded_offsets() {
        final ImageRandomProvider images = new ImageRandomProvider(ASSETS,
                world(134));
        final ImageRandomProvider reloaded = new ImageRandomProvider(ASSETS,
                world(134));
        final Set<Image> startingFrames = new HashSet<>();
        for (int x = 1; x <= 30; x++) {
            final Location at = Location.of(x, 20);
            startingFrames
                    .add(images.getTerrainDecoration(TerrainType.OCEAN, at, 0));
            for (final TerrainType type : new TerrainType[] { TerrainType.OCEAN,
                    TerrainType.HIGH_SEA, TerrainType.LAKE }) {
                final Set<Image> frames = new HashSet<>();
                for (int tick = 0; tick < 180; tick++) {
                    final Image frame = images.getTerrainDecoration(type, at,
                            tick);
                    frames.add(frame);
                    assertSame(frame,
                            images.getTerrainDecoration(type, at, tick + 180));
                    assertSame(frame,
                            reloaded.getTerrainDecoration(type, at, tick));
                    assertTrue(images.isTerrainDecorated(TerrainType.HIGH_SEA,
                            at, false, tick));
                }
                assertEquals(120, frames.size(),
                        "One complete wave cycle: " + type);
            }
            assertTrue(images.isTerrainDecorated(TerrainType.HIGH_SEA, at,
                    false, Long.MAX_VALUE));
            assertFalse(images.isTerrainDecorated(TerrainType.HIGH_SEA, at,
                    true, 0), "Meaningful overlays retain priority");
            assertFalse(
                    images.isTerrainDecorated(TerrainType.LAKE, at, true, 0),
                    "Fish and other meaningful overlays retain priority");
        }
        assertTrue(startingFrames.size() > 10,
                "Neighbouring water must not animate in lockstep");
    }

    @Test
    void water_placement_never_cuts_an_animation_cycle_short() {
        final ImageRandomProvider images = new ImageRandomProvider(ASSETS,
                world(134));
        final WorldMapRandoms randoms = new WorldMapRandoms(world(134));
        for (final TerrainType type : new TerrainType[] { TerrainType.OCEAN,
                TerrainType.LAKE }) {
            int decorated = 0;
            for (int x = 1; x <= 100; x++) {
                final Location at = Location.of(x, 20);
                final long start = Math
                        .floorMod(-(long) randoms.getRandomAt(at), 180);
                final boolean visible = images.isTerrainDecorated(type, at,
                        false, start);
                decorated += visible ? 1 : 0;
                for (int tick = 0; tick < 180; tick++) {
                    assertEquals(visible,
                            images.isTerrainDecorated(type, at, false,
                                    start + tick),
                            "Selection remains fixed through the entire fall and dissolve");
                }
            }
            assertTrue(decorated > 0 && decorated < 20,
                    "Exercise both selected and absent waves");
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
                        false, 180))
                    changedWater++;
                if (water != first.isTerrainDecorated(TerrainType.OCEAN, at,
                        false, 1))
                    nextFrameChanges++;
                if (first.isTerrainDecorated(TerrainType.OCEAN, at, false, 180))
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
