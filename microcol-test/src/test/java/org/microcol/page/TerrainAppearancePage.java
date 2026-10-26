package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Location;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

/**
 * Checks the shared map renderer without relying on wall-clock animation
 * timing.
 */
public final class TerrainAppearancePage extends AbstractScreen {
    private Map<Location, Boolean> remembered;

    public TerrainAppearancePage(final TestContext context) {
        super(context);
    }

    private Map<Location, Boolean> landLayout(final long tick) {
        final Map<Location, Boolean> result = new LinkedHashMap<>();
        final MapManager map = getContext().getClassFromGuice(MapManager.class);
        for (int y = 1; y <= getModel().getMap().getMaxLocationY(); y++) {
            for (int x = 1; x <= getModel().getMap().getMaxLocationX(); x++) {
                final Location at = Location.of(x, y);
                final Terrain terrain = getModel().getMap().getTerrainAt(at);
                if (terrain.getTerrainType().isLand()) {
                    result.put(at, map.isTerrainDecorated(at, terrain, tick));
                }
            }
        }
        return result;
    }

    public void rememberLandDecoration() throws Exception {
        remembered = WaitForAsyncUtils.asyncFx(() -> landLayout(0)).get(5,
                TimeUnit.SECONDS);
    }

    public void verifyLandDecorationUnchanged() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            assertEquals(remembered, landLayout(9000),
                    "Panning and Continue must preserve land decoration");
        }).get(5, TimeUnit.SECONDS);
    }

    private Image render(final Location at, final Terrain terrain,
            final long tick) {
        final Canvas canvas = new Canvas(45, 45);
        getContext().getClassFromGuice(PaintService.class).paintTerrainOnTile(
                canvas.getGraphicsContext2D(), Point.of(0, 0), at, terrain,
                false, tick);
        return canvas.snapshot(null, null);
    }

    public void verifySparseTerrainAndMovingWater() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final MapManager map = getContext()
                    .getClassFromGuice(MapManager.class);
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            int bare = 0;
            int decorated = 0;
            int movingWater = 0;
            Location decoratedAt = null;
            for (int x = 1; x <= getModel().getMap().getMaxLocationX(); x++) {
                final Location at = Location.of(x, 20);
                if (map.getCoatsImageAt(at) != null)
                    continue;
                final Terrain grass = new Terrain(at, TerrainType.GRASSLAND);
                final boolean visible = map.isTerrainDecorated(at, grass, 0);
                final Canvas expected = new Canvas(45, 45);
                expected.getGraphicsContext2D()
                        .setFill(images.getTerrainColor(TerrainType.GRASSLAND));
                expected.getGraphicsContext2D().fillRect(0, 0, 45, 45);
                if (visible) {
                    decorated++;
                    decoratedAt = at;
                    expected.getGraphicsContext2D().drawImage(
                            images.getTerrainImage(TerrainType.GRASSLAND), 0,
                            0);
                } else {
                    bare++;
                }
                verifyPixels(expected.snapshot(null, null),
                        render(at, grass, 0));
                final Terrain water = new Terrain(at, TerrainType.OCEAN);
                final Image start = render(at, water, 0);
                for (int tick = 120; tick <= 1200; tick += 120) {
                    if (!samePixels(start, render(at, water, tick))) {
                        movingWater++;
                        break;
                    }
                }
            }
            assertTrue(bare > decorated && decorated > 0,
                    "Grass must have both sparse symbols and plain coloured squares");
            assertTrue(movingWater > 0,
                    "Actual water rendering must change across time");
            final Terrain forest = new Terrain(decoratedAt,
                    TerrainType.GRASSLAND);
            forest.setHasTrees(true);
            assertFalse(map.isTerrainDecorated(decoratedAt, forest, 0),
                    "Forest suppresses decoration");
            forest.setHasTrees(false);
            forest.setHasField(true);
            assertFalse(map.isTerrainDecorated(decoratedAt, forest, 0),
                    "Field suppresses decoration");
            forest.setHasField(false);
            forest.setHasRoad(true);
            assertFalse(map.isTerrainDecorated(decoratedAt, forest, 0),
                    "Road suppresses decoration");
            forest.setHasRoad(false);
            forest.setResourceOverlay(ResourceOverlay.PRIME_TOBACCO);
            assertFalse(map.isTerrainDecorated(decoratedAt, forest, 0),
                    "Bonus suppresses decoration");
            final Canvas bonus = new Canvas(45, 45);
            bonus.getGraphicsContext2D()
                    .setFill(images.getTerrainColor(TerrainType.GRASSLAND));
            bonus.getGraphicsContext2D().fillRect(0, 0, 45, 45);
            bonus.getGraphicsContext2D().drawImage(
                    images.getResourceImage(ResourceOverlay.PRIME_TOBACCO), 0,
                    0);
            verifyPixels(bonus.snapshot(null, null),
                    render(decoratedAt, forest, 0));
            final Location colony = getModel().getColonyById(0).orElseThrow()
                    .getLocation();
            assertFalse(
                    map.isTerrainDecorated(colony,
                            new Terrain(colony, TerrainType.GRASSLAND), 0),
                    "Colony suppresses decoration");
            for (final TerrainType relief : new TerrainType[] {
                    TerrainType.HILL, TerrainType.MOUNTAIN }) {
                final Terrain terrain = new Terrain(decoratedAt, relief);
                terrain.setResourceOverlay(ResourceOverlay.ORE);
                assertTrue(map.isTerrainDecorated(decoratedAt, terrain, 0),
                        "Relief remains with a bonus");
                final Canvas expected = new Canvas(45, 45);
                expected.getGraphicsContext2D()
                        .drawImage(images.getTerrainImage(relief), 0, 0);
                expected.getGraphicsContext2D().drawImage(
                        images.getResourceImage(ResourceOverlay.ORE), 0, 0);
                verifyPixels(expected.snapshot(null, null),
                        render(decoratedAt, terrain, 0));
            }
        }).get(15, TimeUnit.SECONDS);
    }

    public void verifyHighSeasWavesAndStaticPreview() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final MapManager map = getContext()
                    .getClassFromGuice(MapManager.class);
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            int checked = 0;
            for (int x = 1; x <= getModel().getMap().getMaxLocationX()
                    && checked < 3; x++) {
                final Location at = Location.of(x, 20);
                if (map.getCoatsImageAt(at) != null
                        || map.getHiddenImageCoast(at) != null)
                    continue;
                final Terrain sea = new Terrain(at, TerrainType.HIGH_SEA);
                final Image crest = images.getImage("wave-HIGH_SEA-53");
                int crestTick = -1;
                for (int tick = 0; tick < 180; tick++) {
                    assertTrue(map.isTerrainDecorated(at, sea, tick),
                            "Every eligible High Seas tile participates in the complete cycle");
                    if (map.getTerrainDecoration(TerrainType.HIGH_SEA, at,
                            tick) == crest)
                        crestTick = tick;
                }
                assertTrue(crestTick >= 0,
                        "The six-second cycle contains the approved crest");
                final int[] frames = { 53, 59, 98 };
                final int[] offsets = { 0, 9, 67 };
                for (int phase = 0; phase < frames.length; phase++) {
                    final Canvas expected = new Canvas(45, 45);
                    expected.getGraphicsContext2D().setFill(
                            images.getTerrainColor(TerrainType.HIGH_SEA));
                    expected.getGraphicsContext2D().fillRect(0, 0, 45, 45);
                    expected.getGraphicsContext2D().drawImage(
                            images.getImage("wave-HIGH_SEA-" + frames[phase]),
                            0, 0);
                    verifyPixels(expected.snapshot(null, null),
                            render(at, sea, crestTick + offsets[phase]));
                }
                assertFalse(
                        samePixels(render(at, sea, crestTick),
                                render(at, sea, crestTick + 9)),
                        "The overhang falls forward into the surface");
                assertFalse(
                        samePixels(render(at, sea, crestTick + 9),
                                render(at, sea, crestTick + 67)),
                        "The impact dissolves into the surface");
                verifyPixels(render(at, sea, 0), render(at, sea, 180));
                final Canvas stationary = new Canvas(45, 45);
                getContext().getClassFromGuice(PaintService.class)
                        .paintTerrainOnTile(stationary.getGraphicsContext2D(),
                                Point.of(0, 0), at, sea, false);
                verifyPixels(render(at, sea, crestTick),
                        stationary.snapshot(null, null));
                checked++;
            }
            assertTrue(checked > 0,
                    "The scenario exercises stationary and animated High Seas");
        }).get(15, TimeUnit.SECONDS);
    }

    private void verifyPixels(final Image expected, final Image actual) {
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                final int wanted = expected.getPixelReader().getArgb(x, y);
                final int found = actual.getPixelReader().getArgb(x, y);
                for (int shift = 0; shift <= 24; shift += 8) {
                    assertEquals((wanted >>> shift) & 255,
                            (found >>> shift) & 255, 1,
                            "Painted terrain pixel at " + x + "," + y);
                }
            }
        }
    }

    private boolean samePixels(final Image first, final Image second) {
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                if (first.getPixelReader().getArgb(x, y) != second
                        .getPixelReader().getArgb(x, y))
                    return false;
            }
        }
        return true;
    }
}
