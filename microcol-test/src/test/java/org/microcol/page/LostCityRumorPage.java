package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.model.Location;
import org.microcol.model.turnevent.TurnEventLostCityRumor;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Inspect lost-city rumors through the live model and the rendered map. The
 * model answers what the game holds; the canvas answers what the player sees.
 */
public final class LostCityRumorPage extends AbstractScreen {

    /** Fill of the marker, the amber triangle in a tile's lower left corner. */
    private static final Color MARKER_FILL = Color.web("#E8B84A");

    /** A pixel well inside the marker triangle of a 45 px tile. */
    private static final int MARKER_X = 8;

    private static final int MARKER_Y = 37;

    private static final String HUMAN = "Dutch";

    public LostCityRumorPage(final TestContext context) {
        super(context);
    }

    /** The map still (or no longer) holds an unresolved rumor on the square. */
    public void verifyRumorMarker(final Location location,
            final boolean expected) {
        assertEquals(expected, getModel().getMap().hasLostCityRumorAt(location),
                "Rumor marker state at " + location);
    }

    /**
     * The player sees (or does not see) the marker on the rendered map. The
     * canvas is redrawn on the next frame after a model change, so the check
     * waits for that frame, up to a limit, before it decides.
     */
    public void verifyRumorMarkerDrawn(final Location location,
            final boolean expected) throws Exception {
        assertTrue(getContext().getArea().isLocationVisible(location),
                "The square must be on screen: " + location);
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> closeTo(MARKER_FILL,
                            markerSample(location)) == expected);
        } catch (final java.util.concurrent.TimeoutException timeout) {
            // The final sample below explains the difference.
        }
        final Color sample = markerSample(location);
        assertEquals(expected, closeTo(MARKER_FILL, sample),
                "Marker pixel at " + location + " was " + sample);
    }

    /**
     * The status bar, which describes the square under the mouse, names (or
     * does not name) a lost-city rumor there in the current language.
     */
    public void verifyStatusBarNamesRumor(final String rumorLabel,
            final boolean expected) {
        WaitForAsyncUtils.waitForFxEvents();
        final String text = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID)
                .getText();
        assertEquals(expected, text.contains(rumorLabel),
                "Status bar: " + text);
    }

    private Color markerSample(final Location location) throws Exception {
        final Image actual = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        return actual.getPixelReader().getColor(tile.getX() + MARKER_X,
                tile.getY() + MARKER_Y);
    }

    /** The Dutch treasury holds exactly this much gold. */
    public void verifyTreasury(final int expected) {
        assertEquals(expected, getModel().getPlayerByName(HUMAN).getGold());
    }

    public void verifyScoutAt(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    /** The unit is no longer part of the game. */
    public void verifyScoutGone(final int unitId) {
        assertTrue(
                getModel().getAllUnits().stream()
                        .noneMatch(unit -> unit.getId() == unitId),
                "Unit " + unitId + " must be gone");
    }

    /** The number of saved rumor notices the Dutch player still holds. */
    public void verifyRumorNoticeCount(final int expected) {
        assertEquals(expected, getModel()
                .getTurnEventsLocalizedMessages(
                        getModel().getPlayerByName(HUMAN))
                .stream().filter(TurnEventLostCityRumor.class::isInstance)
                .count());
    }

    /** No full-screen report covers the map. */
    public void verifyNoReportShown() {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(
                getNodeFinder().lookup("#closeButtonId").tryQuery().isPresent(),
                "No report may be shown");
    }

    /** The Dutch units standing on the square, scouts and recruits together. */
    public void verifyUnitsAt(final Location location, final int expected) {
        assertEquals(expected, getModel().getUnitsAt(location).size());
    }

    /**
     * Keep the rendered map as review evidence under target/warehouse-qa. The
     * file is written only; it makes no claim about the map.
     */
    public void saveMapEvidence(final String fileName) throws Exception {
        new FogPage(getContext()).saveMapEvidence(fileName);
    }

    private static boolean closeTo(final Color expected, final Color actual) {
        return Math.abs(expected.getRed() - actual.getRed()) < 0.06
                && Math.abs(expected.getGreen() - actual.getGreen()) < 0.06
                && Math.abs(expected.getBlue() - actual.getBlue()) < 0.06;
    }

}
