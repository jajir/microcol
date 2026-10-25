package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/** Real Disband control, its confirmation and the visible results. */
public final class DisbandPage extends AbstractScreen {

    private static final String CONTROL_ID = "disbandUnit";

    public DisbandPage(final TestContext context) {
        super(context);
    }

    /** Use the visible Disband control of the selected unit. */
    public void useControl() {
        clickOnButtonWithId(CONTROL_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void acknowledgeExcess() {
        getRobot().clickOn("#disbandOverflowAcknowledge");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("disbandConfirm");
    }

    public void cancel() {
        clickOnButtonWithId("disbandCancel");
    }

    /**
     * The control stays in the panel and is hidden when no own unit is
     * selected, so presence alone does not mean it is shown.
     */
    private boolean isShown(final String cssId) {
        if (!cssId.equals("goToStatus")) {
            openUnitActions();
        }
        return getNodeFinder().lookup("#" + cssId).<Node>tryQuery()
                .map(node -> node.isVisible() && node.isManaged())
                .orElse(false);
    }

    private Optional<Unit> findUnit(final int id) {
        return getModel().tryGetUnitById(id);
    }

    private static UnitFreeColonist colonist(final Unit unit) {
        return (UnitFreeColonist) unit;
    }

    public void verifyControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isShown(CONTROL_ID),
                "Unexpected Disband control visibility");
        if (!expectedVisible) {
            assertFalse(isCssIdShown("unitActions"),
                    "No own unit has an action menu");
        }
        closeUnitActions();
    }

    public void verifyControlEnabled(final boolean expectedEnabled) {
        final Button button = getButtonById(CONTROL_ID);
        assertEquals(expectedEnabled, !button.isDisabled(),
                "Unexpected Disband control state");
        closeUnitActions();
    }

    public void verifyConfirmation(final String expectedTitle,
            final String expectedQuestion, final String expectedDestination) {
        assertEquals(expectedTitle, getNodeFinder().lookup("#disbandTitle")
                .queryAs(Label.class).getText());
        assertEquals(expectedQuestion, getNodeFinder()
                .lookup("#disbandQuestion").queryAs(Label.class).getText());
        assertEquals(expectedDestination, getNodeFinder()
                .lookup("#disbandDestination").queryAs(Label.class).getText());
    }

    public void verifyEquipmentLines(final String expectedTools,
            final String expectedHorses, final String expectedMuskets) {
        assertEquals(expectedTools, getNodeFinder().lookup("#disbandToolsLine")
                .queryAs(Label.class).getText());
        assertEquals(expectedHorses, getNodeFinder()
                .lookup("#disbandHorsesLine").queryAs(Label.class).getText());
        assertEquals(expectedMuskets, getNodeFinder()
                .lookup("#disbandMusketsLine").queryAs(Label.class).getText());
    }

    public void verifyOverflowAcknowledgement(final boolean expectedShown) {
        assertEquals(expectedShown, isShown("disbandOverflowAcknowledge"),
                "Unexpected overflow acknowledgement visibility");
        closeUnitActions();
    }

    public void verifyConfirmEnabled(final boolean expectedEnabled) {
        final Button confirm = getNodeFinder().lookup("#disbandConfirm")
                .queryAs(Button.class);
        assertEquals(expectedEnabled, !confirm.isDisabled(),
                "Unexpected confirmation state");
    }

    public void verifyDialogClosed() {
        assertTrue(
                getNodeFinder().lookup("#disbandQuestion").tryQuery().isEmpty(),
                "The confirmation must be closed");
    }

    public void verifyUnitPresent(final int id, final boolean expectedPresent) {
        assertEquals(expectedPresent, findUnit(id).isPresent(),
                "Unexpected presence of unit " + id);
    }

    /** Equipment is checked on a unit that is still in the game. */
    public void verifyEquipment(final int id, final int expectedTools,
            final boolean expectedMounted, final boolean expectedHoldingGuns) {
        final UnitFreeColonist unit = colonist(getModel().getUnitById(id));
        assertEquals(expectedTools, unit.getTools(),
                "Unexpected tools of unit " + id);
        assertEquals(expectedMounted, unit.isMounted(),
                "Unexpected horses of unit " + id);
        assertEquals(expectedHoldingGuns, unit.isHoldingGuns(),
                "Unexpected muskets of unit " + id);
    }

    public void verifyPassengerAboard(final int passengerId,
            final int carrierId) {
        final Unit passenger = getModel().getUnitById(passengerId);
        assertTrue(passenger.isAtCargoSlot(),
                "Passenger " + passengerId + " must stay aboard");
        assertEquals(carrierId,
                passenger.getPlaceCargoSlot().getOwnerUnit().getId(),
                "Unexpected carrier of passenger " + passengerId);
    }

    public void verifyColonyStock(final int colonyId, final GoodsType type,
            final int expectedAmount) {
        assertEquals(expectedAmount,
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(type).getAmount(),
                "Unexpected " + type + " in colony " + colonyId);
    }

    /** The map selection holds the expected unit, or none. */
    public void verifySelectedUnit(final Optional<Integer> expectedId) {
        final SelectedUnitManager selection = getContext()
                .getClassFromGuice(SelectedUnitManager.class);
        assertEquals(expectedId, selection.getSelectedUnit().map(Unit::getId),
                "Unexpected selected unit");
    }
}
