package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.microcol.model.Location;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeTeachingCourse;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.input.KeyCode;

/**
 * The native course after it has started (R-0148): the settlement details that
 * show its progress and the stop control, the owner turns that count its
 * updates, and the model state those turns leave behind. Actions only click or
 * press keys; every assertion lives in a verify method.
 */
public final class NativeTeachingPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    public NativeTeachingPage(final TestContext context) {
        super(context);
    }

    /** Open the native settlements details with the N key. */
    public void openSettlements() {
        getRobot().push(KeyCode.N);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Press the stop control of the open details; it ends the course. */
    public void stopLearning() {
        getRobot().clickOn(stopButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the details with Escape, leaving the course as it is. */
    public void closeSettlements() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** One European turn end; the turn report, if any, is closed. */
    public void nextTurn() throws Exception {
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * One European turn end that is expected to leave a turn report; the report
     * stays open for the checks and capture that follow.
     */
    public ScreenTurnReport nextTurnWithReport() throws Exception {
        return GamePage.of(getContext()).nextTurn().orElseThrow();
    }

    /** Capture the open turn report. */
    public void captureReport(final String fileName) throws Exception {
        captureDialog("closeButtonId", fileName);
    }

    /** Close the report that {@link #nextTurnWithReport()} returned. */
    public void closeReport(final ScreenTurnReport report) {
        report.close();
    }

    /** The turn report announces the outcome of the course. */
    public void verifyReportAnnounces(final ScreenTurnReport report,
            final String expectedPart) {
        report.verifyThatAtLeastOneEventMessageContains(expectedPart);
    }

    private javafx.scene.control.Button stopButton() {
        return getNodeFinder()
                .lookup("#" + NativeSettlementsDialog.STOP_TEACHING_ID)
                .queryButton();
    }

    /** Capture the details dialog with the course line visible. */
    public void captureSettlements(final String fileName) throws Exception {
        captureDialog(NativeSettlementsDialog.CONTENT_ID, fileName);
    }

    /** The details show the progress line of the learning course. */
    public void verifyProgress(final String expected) {
        assertTrue(isCssIdShown(NativeSettlementsDialog.TEACHING_ID));
        assertEquals(expected,
                getLabeledById(NativeSettlementsDialog.TEACHING_ID).getText());
        assertTrue(isCssIdShown(NativeSettlementsDialog.STOP_TEACHING_ID));
    }

    /** The details show the waiting line and the leave control. */
    public void verifyWaiting(final String expected) {
        assertTrue(isCssIdShown(NativeSettlementsDialog.TEACHING_ID));
        assertEquals(expected,
                getLabeledById(NativeSettlementsDialog.TEACHING_ID).getText());
        assertTrue(isCssIdShown(NativeSettlementsDialog.STOP_TEACHING_ID));
    }

    /**
     * The details show no course, so the owner has nothing to stop. The dialog
     * keeps both nodes but hides them, so visibility is checked.
     */
    public void verifyNoCourseShown() {
        assertTrue(isCssIdVisible(NativeSettlementsDialog.CONTENT_ID),
                "Settlement details are not open");
        assertFalse(isCssIdShown(NativeSettlementsDialog.TEACHING_ID));
        assertFalse(isCssIdShown(NativeSettlementsDialog.STOP_TEACHING_ID));
    }

    /**
     * The course is running: the colonist stands in the native course, the
     * settlement names it with the offered skill, and the owner updates counted
     * so far are the expected ones.
     */
    public void verifyLearning(final int unitId, final Location settlement,
            final int completedUpdates) throws Exception {
        final NativeSettlement site = knownSettlement(settlement);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> site.getTeachingUpdates() == completedUpdates);
        assertTrue(getModel().getUnitById(unitId).isAtPlaceNativeCourse());
        assertEquals(unitId, site.getTeachingPupilId().orElseThrow());
        final NativeTeachingCourse course = getModel()
                .getNativeTeachingCourse(site).orElseThrow();
        assertEquals(UnitType.EXPERT_FARMER, course.getSkill());
        assertEquals(NativeTeachingCourse.Stage.LEARNING, course.getStage());
        assertEquals(completedUpdates, course.getCompletedUpdates());
    }

    /**
     * The fourth update granted the skill to the same person, which keeps its
     * identity and its mounted equipment, and released it onto the map.
     */
    public void verifyLearnedAndReleased(final int unitId,
            final Location settlement, final boolean mounted) throws Exception {
        final NativeSettlement site = knownSettlement(settlement);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> site.getTeachingPupilId().isEmpty());
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
        assertEquals(UnitType.EXPERT_FARMER,
                getModel().getUnitById(unitId).getType());
        assertEquals(unitId, getModel().getUnitById(unitId).getId());
        assertEquals(mounted,
                ((UnitFreeColonist) getModel().getUnitById(unitId))
                        .isMounted());
        assertTrue(site.getTeachingPupilId().isEmpty());
    }

    /**
     * Stopping the course released the colonist with its original type and no
     * skill, and the settlement is free again.
     */
    public void verifyStoppedWithoutSkill(final int unitId,
            final Location settlement) throws Exception {
        final NativeSettlement site = knownSettlement(settlement);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> site.getTeachingPupilId().isEmpty());
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
        assertEquals(UnitType.COLONIST,
                getModel().getUnitById(unitId).getType());
        assertTrue(site.getTeachingPupilId().isEmpty());
    }

    private NativeSettlement knownSettlement(final Location settlement) {
        return getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName(DUTCH),
                        settlement)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + settlement));
    }

}
