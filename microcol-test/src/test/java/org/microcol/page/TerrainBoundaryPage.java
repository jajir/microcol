package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.image.Image;

/** Player-visible land boundaries and their persistence in the real map. */
public final class TerrainBoundaryPage extends AbstractScreen {
    private Map<Location, Long> remembered;
    private Map<Location, TerrainType> terrain;

    public TerrainBoundaryPage(final TestContext context) {
        super(context);
    }

    private Map<Location, Long> appearance() {
        final Map<Location, Long> result = new LinkedHashMap<>();
        final MapManager manager = getContext()
                .getClassFromGuice(MapManager.class);
        for (int y = 12; y < 18; y++) {
            for (int x = 14; x < 22; x++) {
                final Location at = Location.of(x, y);
                final Image image = manager.getTerrainBackground(at,
                        getModel().getMap().getTerrainTypeAt(at));
                long fingerprint = 1;
                if (image != null) {
                    for (int py = 0; py < 45; py++) {
                        for (int px = 0; px < 45; px++) {
                            fingerprint = fingerprint * 31
                                    + image.getPixelReader().getArgb(px, py);
                        }
                    }
                }
                result.put(at, fingerprint);
            }
        }
        return result;
    }

    public void rememberBoundaryAppearance() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            remembered = appearance();
            terrain = new LinkedHashMap<>();
            for (final Location at : remembered.keySet()) {
                terrain.put(at, getModel().getMap().getTerrainTypeAt(at));
            }
        }).get(10, TimeUnit.SECONDS);
    }

    public void verifyBoundaryAppearanceUnchanged() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            assertEquals(remembered, appearance(),
                    "Pan and Continue preserve every terrain boundary");
            for (final Map.Entry<Location, TerrainType> entry : terrain
                    .entrySet()) {
                assertEquals(entry.getValue(),
                        getModel().getMap().getTerrainTypeAt(entry.getKey()),
                        "Drawing must not change the saved map's terrain");
            }
        }).get(10, TimeUnit.SECONDS);
    }

    public void verifyIrregularBoundariesOnMap() throws Exception {
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.asyncFx(() -> {
            final MapManager manager = getContext()
                    .getClassFromGuice(MapManager.class);
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            final Image canvas = FogFrameProbe.captureCanvas(getContext());
            int transferred = 0;
            int onScreen = 0;
            for (int y = 12; y < 18; y++) {
                for (int x = 14; x < 22; x++) {
                    final Location at = Location.of(x, y);
                    final TerrainType type = getModel().getMap()
                            .getTerrainTypeAt(at);
                    final Image background = manager.getTerrainBackground(at,
                            type);
                    if (background == null) {
                        continue;
                    }
                    assertEquals(images.getTerrainColor(type),
                            background.getPixelReader().getColor(22, 22),
                            "The centre still identifies the logical square");
                    final Point point = getContext().getArea()
                            .convertToCanvasPoint(at);
                    final int own = background.getPixelReader().getArgb(22, 22);
                    for (int py = 2; py < 43; py++) {
                        for (int px = 2; px < 43; px++) {
                            final int colour = background.getPixelReader()
                                    .getArgb(px, py);
                            if (colour == own) {
                                continue;
                            }
                            transferred++;
                            final int sx = point.getX() + px,
                                    sy = point.getY() + py;
                            if (sx >= 0 && sy >= 0 && sx < canvas.getWidth()
                                    && sy < canvas.getHeight()
                                    && canvas.getPixelReader().getArgb(sx,
                                            sy) == colour) {
                                onScreen++;
                            }
                        }
                    }
                }
            }
            assertTrue(transferred > 20,
                    "Neighbouring colours cross the old straight edges");
            assertTrue(onScreen > 10,
                    "The actual game canvas displays the irregular colour boundaries");
        }).get(10, TimeUnit.SECONDS);
    }
}
