package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;

/**
 * Player gestures and observations for the native advisor (R-0201): the N
 * shortcut, the nations line, the chief line of the selected settlement, the
 * map control and the cancel path. Assertions live only in the verify methods.
 */
public final class NativeAdvisorPage extends AbstractScreen {

    public static NativeAdvisorPage of(final TestContext context) {
        return new NativeAdvisorPage(context);
    }

    private NativeAdvisorPage(final TestContext context) {
        super(context);
    }

    /** Press N on the game screen, the shortcut of the native advisor. */
    public void openWithNKey() throws Exception {
        getRobot().push(KeyCode.N);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> isCssIdVisible(NativeSettlementsDialog.CONTENT_ID));
    }

    /** Escape closes the advisor; it keeps no change. */
    public void closeWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Press the map control of the selected settlement. */
    public void showOnMap() {
        getRobot()
                .clickOn(getButtonById(NativeSettlementsDialog.SHOW_ON_MAP_ID));
    }

    /** Keep the open advisor as review evidence under target/warehouse-qa. */
    public void captureAdvisor(final String fileName) throws IOException {
        captureDialog(NativeSettlementsDialog.CONTENT_ID, fileName);
    }

    public void verifyDialogClosed() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !isCssIdVisible(NativeSettlementsDialog.CONTENT_ID));
        assertFalse(isCssIdVisible(NativeSettlementsDialog.CONTENT_ID),
                "Native advisor should be closed");
    }

    /** The nations line names exactly the nations the player knows. */
    public void verifyNations(final String expected) {
        assertTrue(isCssIdShown(NativeSettlementsDialog.NATIONS_ID),
                "The known nations line is not shown");
        assertEquals(expected,
                getLabeledById(NativeSettlementsDialog.NATIONS_ID).getText(),
                "Unexpected known nations line");
    }

    /** The attitude line of the selected settlement. */
    public void verifyAttitude(final String expected) {
        assertEquals(expected,
                getLabeledById(NativeSettlementsDialog.ATTITUDE_ID).getText(),
                "Unexpected attitude line");
    }

    /**
     * The chief line of the selected settlement: the learned goods and skill,
     * or the statement that the chief has not told anything yet.
     */
    public void verifyChiefLine(final String expected) {
        assertTrue(isCssIdShown(NativeSettlementsDialog.CHIEF_ID),
                "The chief line is not shown");
        assertEquals(expected,
                getLabeledById(NativeSettlementsDialog.CHIEF_ID).getText(),
                "Unexpected chief line");
    }

    /** The map control is shown for the selected settlement. */
    public void verifyShowOnMapShown() {
        assertTrue(isCssIdShown(NativeSettlementsDialog.SHOW_ON_MAP_ID),
                "The show on map control is not shown");
    }

    /**
     * The advisor content lies inside its window at the test viewport, so no
     * line is cut off.
     */
    public void verifyDialogFitsWindow() throws Exception {
        final Node root = getNodeFinder()
                .lookup("#" + NativeSettlementsDialog.CONTENT_ID).query();
        final boolean fits = WaitForAsyncUtils.asyncFx(() -> {
            final Bounds bounds = root.localToScene(root.getBoundsInLocal());
            return bounds.getMinX() >= 0 && bounds.getMinY() >= 0
                    && bounds.getMaxX() <= root.getScene().getWidth()
                    && bounds.getMaxY() <= root.getScene().getHeight();
        }).get(5, TimeUnit.SECONDS);
        assertTrue(fits, "Native advisor must fit its window");
    }

}
