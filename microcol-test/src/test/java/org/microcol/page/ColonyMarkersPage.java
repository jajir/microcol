package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.image.ImageLoaderColonies;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Observe the real markers, including the small overhang at normal map scale.
 */
public final class ColonyMarkersPage extends AbstractScreen {
    public ColonyMarkersPage(final TestContext context) {
        super(context);
    }

    public void verifyMapMarkers() throws Exception {
        final Image frame = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        for (int defense = 0; defense < 4; defense++) {
            for (int size = 0; size < 3; size++) {
                final Location location = Location.of(20 + size * 4,
                        10 + defense * 3);
                final Point point = getContext().getArea()
                        .convertToCanvasPoint(location);
                final org.microcol.model.Colony colony = getModel()
                        .getColonyAt(location).orElseThrow();
                assertEquals(new int[] { 1, 4, 8 }[size],
                        colony.getPopulation());
                assertEquals(defense, colony.getFortificationLevel());
                verifyGlyph(frame, point, size + 1, defense);
            }
        }
    }

    public void verifySidebar(final int houses, final int defense) {
        getRobot().interact(() -> {
            final Canvas canvas = (Canvas) ((javafx.scene.layout.Pane) getNodeFinder()
                    .lookup("#tileImage").query()).getChildren().get(0);
            assertEquals(53, canvas.getWidth());
            verifyGlyph(canvas.snapshot(null, null), Point.of(4, 0), houses,
                    defense);
        });
    }

    public void verifyNationalFlags() {
        getRobot().interact(() -> {
            final ImageProvider provider = getContext()
                    .getClassFromGuice(ImageProvider.class);
            for (final EuropeanNation nation : EuropeanNation.values()) {
                final Image flag = provider
                        .getImage(ImageLoaderColonies.flagKey(nation));
                assertEquals(128, flag.getWidth());
                assertTrue(countColor(flag, 88, 26, 20, 16,
                        Color.web("#bd5f50")) > 4);
                assertTrue(countColor(flag, 88, 26, 20, 16,
                        Color.web(nation == EuropeanNation.SPANISH ? "#dfbd58"
                                : "#fff9ec")) > 4);
                if (nation == EuropeanNation.DUTCH
                        || nation == EuropeanNation.FRENCH) {
                    assertTrue(countColor(flag, 88, 26, 20, 16,
                            Color.web("#376aa0")) > 4);
                }
            }
        });
    }

    private void verifyGlyph(final Image frame, final Point point,
            final int houses, final int defense) {
        final int x = point.getX() - 4;
        final int y = point.getY();
        assertEquals(houses, roofCount(frame, x, y),
                "Distinct roofs at " + point);
        assertTrue(countColor(frame, x, y, 53, 44, Color.web("#fff9ec")) > 20,
                "Paper house bodies");
        assertTrue(countColor(frame, x, y, 53, 44, Color.web("#376aa0")) > 2,
                "Dutch flag on the map");
        if (defense >= 2) {
            assertTrue(
                    countColor(frame, x, y, 53, 44,
                            Color.web(
                                    defense == 2 ? "#b3afa0" : "#a29a8d")) > 15,
                    "Stone fortification");
        } else if (defense == 1) {
            assertTrue(
                    countColor(frame, x, y, 53, 44, Color.web("#815b3c")) > 4,
                    "Wooden palisade");
        }
    }

    private static int countColor(final Image image, final int left,
            final int top, final int width, final int height,
            final Color color) {
        int count = 0;
        for (int y = top; y < top + height; y++) {
            for (int x = left; x < left + width; x++) {
                if (near(image.getPixelReader().getColor(x, y), color))
                    count++;
            }
        }
        return count;
    }

    private static boolean near(final Color a, final Color b) {
        return Math.abs(a.getRed() - b.getRed()) < .06
                && Math.abs(a.getGreen() - b.getGreen()) < .06
                && Math.abs(a.getBlue() - b.getBlue()) < .06
                && a.getOpacity() > .9;
    }

    private static int roofCount(final Image image, final int left,
            final int top) {
        final boolean[][] roof = new boolean[44][53];
        for (int y = 0; y < 44; y++) {
            for (int x = 0; x < 53; x++) {
                roof[y][x] = near(
                        image.getPixelReader().getColor(left + x, top + y),
                        Color.web("#c18150"));
            }
        }
        int count = 0;
        for (int y = 0; y < 44; y++) {
            for (int x = 0; x < 53; x++) {
                if (!roof[y][x])
                    continue;
                final ArrayDeque<Integer> pending = new ArrayDeque<>();
                pending.add(y * 53 + x);
                roof[y][x] = false;
                int pixels = 0;
                while (!pending.isEmpty()) {
                    final int cell = pending.remove();
                    pixels++;
                    final int cx = cell % 53, cy = cell / 53;
                    for (final int[] step : new int[][] { { 1, 0 }, { -1, 0 },
                            { 0, 1 }, { 0, -1 } }) {
                        final int nx = cx + step[0], ny = cy + step[1];
                        if (nx >= 0 && nx < 53 && ny >= 0 && ny < 44
                                && roof[ny][nx]) {
                            roof[ny][nx] = false;
                            pending.add(ny * 53 + nx);
                        }
                    }
                }
                if (pixels > 4)
                    count++;
            }
        }
        return count;
    }
}
