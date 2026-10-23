package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microcol.gui.dialog.DeclareIndependenceDialog;
import org.microcol.gui.screen.europe.EuropeButtonsPanel;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.model.ConcurrentModelEvents;
import org.microcol.model.Location;
import org.microcol.model.MilitaryRole;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.input.KeyCode;

/**
 * The independence button, its confirmation and the observable consequences in
 * the model, through the ordinary game controls.
 */
public final class IndependencePage extends AbstractScreen {

    private static final String PLAYER = "Dutch";

    private int unitCount;

    public IndependencePage(final TestContext context) {
        super(context);
    }

    /** Press the declare button; the confirmation opens before any change. */
    public void openConfirmation() {
        clickOnButtonWithId(ButtonsGamePanel.BUTTON_DECLARE_INDEPENDENCE_ID);
    }

    public void confirm() {
        clickOnButtonWithId(DeclareIndependenceDialog.CONFIRM_ID);
    }

    public void cancel() {
        clickOnButtonWithId(DeclareIndependenceDialog.CANCEL_ID);
    }

    public void pressEscape() {
        getRobot().press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Let the calendar pass a boundary while the confirmation stays open. */
    public void advanceCalendarBehindOpenConfirmation() {
        WaitForAsyncUtils.asyncFx(
                () -> ConcurrentModelEvents.advanceCalendar(getModel()));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** The button is offered only while the panel contains it. */
    public void verifyDeclareButtonVisible(final boolean expectedVisible) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expectedVisible,
                isCssIdVisible(ButtonsGamePanel.BUTTON_DECLARE_INDEPENDENCE_ID),
                "Unexpected declare button availability");
    }

    public void verifyConfirmationShown(final String expectedSupport,
            final String expectedMuster) {
        assertTrue(isCssIdVisible(DeclareIndependenceDialog.DIALOG_ID),
                "The confirmation must be open");
        assertEquals(expectedSupport,
                getLabeledById("declareIndependenceSupport").getText());
        assertEquals(expectedMuster,
                getLabeledById("declareIndependenceMuster").getText());
        assertFalse(
                getLabeledById("declareIndependenceEurope").getText().isBlank(),
                "The Europe closure must be explained");
        assertFalse(getLabeledById("declareIndependenceIrreversible").getText()
                .isBlank(), "The permanence must be explained");
        assertTrue(isCssIdVisible(DeclareIndependenceDialog.CONFIRM_ID));
        assertTrue(isCssIdVisible(DeclareIndependenceDialog.CANCEL_ID));
    }

    public void verifyConfirmationClosed() {
        assertFalse(isCssIdVisible(DeclareIndependenceDialog.DIALOG_ID),
                "The confirmation must be closed");
    }

    /**
     * The confirmation warns that home Europe closes before it is confirmed.
     */
    public void verifyEuropeWarning(final String expectedWarning) {
        assertTrue(isCssIdVisible(DeclareIndependenceDialog.DIALOG_ID),
                "The confirmation must be open");
        assertEquals(expectedWarning,
                getLabeledById("declareIndependenceEurope").getText());
    }

    /**
     * The open Europe screen offers neither hiring nor purchases of a closed
     * home port (R-0187).
     */
    public void verifyHomeEuropeClosed() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(getButtonById(EuropeButtonsPanel.BUY_BUTTON_ID).isDisabled(),
                "Buying units must be unavailable in a closed home port");
        assertTrue(getButtonById("recruitButtonId").isDisabled(),
                "Recruitment must be unavailable in a closed home port");
    }

    /** The open Europe screen still offers hiring before a declaration. */
    public void verifyHomeEuropeOpen() {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(
                getButtonById(EuropeButtonsPanel.BUY_BUTTON_ID).isDisabled(),
                "Buying units must be available in an open home port");
    }

    /** The open confirmation explains a changed state and cannot confirm. */
    public void verifyConfirmationRefused(final String expectedExplanation) {
        assertTrue(isCssIdVisible(DeclareIndependenceDialog.DIALOG_ID));
        assertEquals(expectedExplanation,
                getLabeledById("declareIndependenceFeedback").getText());
        assertTrue(getButtonById(DeclareIndependenceDialog.CONFIRM_ID)
                .isDisabled(), "Confirmation must be disabled");
    }

    public void verifyDeclared(final boolean expectedDeclared) {
        assertEquals(expectedDeclared,
                getModel().getPlayerByName(PLAYER).isDeclaredIndependence());
    }

    /** The units that hold a special role, by ascending ID. */
    public void verifyMustered(final Integer... expectedIds) {
        final List<Integer> actual = new ArrayList<>();
        getModel().getAllUnits().stream()
                .filter(UnitFreeColonist.class::isInstance)
                .map(UnitFreeColonist.class::cast)
                .filter(unit -> unit.getOwner().isHuman())
                .filter(unit -> unit
                        .getMilitaryRole() == MilitaryRole.CONTINENTAL)
                .forEach(unit -> actual.add(unit.getId()));
        actual.sort(Integer::compare);
        assertEquals(Arrays.asList(expectedIds), actual);
    }

    public void verifyContinental(final int unitId) {
        assertEquals(MilitaryRole.CONTINENTAL,
                colonist(unitId).getMilitaryRole(), "Unit " + unitId);
    }

    public void verifyUnit(final int unitId, final UnitType expectedType,
            final boolean expectedArmed, final boolean expectedMounted,
            final boolean expectedVeteran, final Location expectedLocation) {
        final UnitFreeColonist unit = colonist(unitId);
        assertEquals(expectedType, unit.getType(), "Type of " + unitId);
        assertEquals(expectedArmed, unit.isHoldingGuns(),
                "Muskets of " + unitId);
        assertEquals(expectedMounted, unit.isMounted(), "Horses of " + unitId);
        assertEquals(expectedVeteran, unit.isVeteran(), "Rank of " + unitId);
        assertEquals(expectedLocation, unit.getLocation(),
                "Place of " + unitId);
    }

    /** Remember how many units exist, before a declaration is attempted. */
    public void rememberUnitCount() {
        unitCount = getModel().getAllUnits().size();
    }

    public void verifyUnitCountUnchanged() {
        assertEquals(unitCount, getModel().getAllUnits().size(),
                "Declaring must not create or remove people");
    }

    private UnitFreeColonist colonist(final int unitId) {
        return (UnitFreeColonist) getModel().getUnitById(unitId);
    }
}
