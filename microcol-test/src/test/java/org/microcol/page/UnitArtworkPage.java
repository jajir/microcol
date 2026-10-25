package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Observes the actual map and shared unit artwork, including equipment states.
 */
public final class UnitArtworkPage extends AbstractScreen {
    public UnitArtworkPage(final TestContext context) {
        super(context);
    }

    public void verifyMapArtwork() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final Image frame = FogFrameProbe.captureCanvas(getContext());
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            for (int id = 1000; id < 1040; id++) {
                final Unit unit = getModel().getUnitById(id);
                final Image sprite = images.getUnitImage(unit);
                final Point point = getContext().getArea()
                        .convertToCanvasPoint(unit.getLocation());
                int expected = 0;
                int matched = 0;
                for (int y = 0; y < 45; y++) {
                    for (int x = 0; x < 45; x++) {
                        final Color color = sprite.getPixelReader().getColor(x,
                                y);
                        // The separately painted national/action flag covers
                        // this corner.
                        if (color.getOpacity() > .99 && !(x < 10 && y < 18)) {
                            expected++;
                            final Color actual = frame.getPixelReader()
                                    .getColor(point.getX() + x,
                                            point.getY() + y);
                            if (Math.abs(actual.getRed() - color.getRed()) < .08
                                    && Math.abs(actual.getGreen()
                                            - color.getGreen()) < .08
                                    && Math.abs(actual.getBlue()
                                            - color.getBlue()) < .08) {
                                matched++;
                            }
                        }
                    }
                }
                assertTrue(expected > 70,
                        unit.getType().name() + " visible artwork");
                assertTrue(matched >= expected * .88,
                        unit.getId() + ": actual map matches "
                                + unit.getType().name() + " " + matched + "/"
                                + expected);
            }
            return null;
        }).get(10, TimeUnit.SECONDS);
    }

    public void verifyUnitLocation(final int id, final Location expected) {
        assertEquals(expected, getModel().getUnitById(id).getLocation());
    }

    public void verifyRosterAndTurn() {
        assertEquals(40, getModel().getAllUnits().size());
        assertEquals(0, getModel().getCalendar().getNumberOfPlayedTurns());
        assertEquals(484, getModel().getPlayerByName("Dutch").getGold());
    }
}
