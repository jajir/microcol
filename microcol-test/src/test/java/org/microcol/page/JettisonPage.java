package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Goods;
import org.microcol.model.Location;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;

/**
 * Real jettison controls on the map and in Europe, and their visible results.
 */
public final class JettisonPage extends AbstractScreen {

    private static final String MAP_CONTROL_ID = "jettisonUnit";

    public JettisonPage(final TestContext context) {
        super(context);
    }

    /** Use the Jettison goods control of the selected ship on the map. */
    public void useMapControl() {
        clickOnButtonWithId(MAP_CONTROL_ID);
    }

    /**
     * Use the Jettison goods control of one ship in the Europe transit list.
     */
    public void useEuropeControl(final int shipId) {
        new EuropeHarborPage(getContext()).openVoyageOrders(shipId, false);
        getRobot().clickOn("#voyage-jettison-" + shipId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Pick one goods hold from the list; a single goods hold skips the list.
     */
    public void chooseHold(final int holdNumber) {
        getRobot().clickOn("#jettisonHold-" + holdNumber);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelHold() {
        getRobot().clickOn("#jettisonCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Move the quantity slider down from the full amount, one goods per press.
     */
    public void lowerQuantity(final int steps) {
        final Slider slider = getNodeFinder().lookup("#goodsTransferSlider")
                .queryAs(Slider.class);
        getRobot().interact(slider::requestFocus);
        for (int step = 0; step < steps; step++) {
            getRobot().push(KeyCode.LEFT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void escapeQuantity() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmQuantity() {
        clickOnButtonWithId("buttonOk");
    }

    public void confirm() {
        getRobot().clickOn("#jettisonConfirm");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelConfirm() {
        getRobot().clickOn("#jettisonCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void escapeConfirm() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeMessage() {
        clickOnButtonWithId("buttonOk");
    }

    /**
     * The control stays in the panel and is hidden when no own ship is
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

    public void verifyMapControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isShown(MAP_CONTROL_ID),
                "Unexpected Jettison goods control visibility");
        closeUnitActions();
    }

    public void verifyMapControlEnabled(final boolean expectedEnabled) {
        assertEquals(expectedEnabled,
                !getButtonById(MAP_CONTROL_ID).isDisabled(),
                "Unexpected Jettison goods control state");
        closeUnitActions();
    }

    public void verifyEuropeControl(final int shipId,
            final boolean expectedEnabled) {
        assertEquals(expectedEnabled,
                !new EuropeHarborPage(getContext())
                        .voyageOrder(shipId, "jettison").isDisable(),
                "Unexpected Europe Jettison goods control state");
    }

    public void verifyHoldChoice(final String expectedQuestion) {
        assertEquals(expectedQuestion,
                getNodeFinder().lookup("#jettisonHoldQuestion")
                        .queryAs(Label.class).getText());
    }

    public void verifyQuantityExplanation(final String expected) {
        assertEquals(expected,
                getNodeFinder().lookup("#goodsTransferExplanation")
                        .queryAs(Label.class).getText());
    }

    public void verifyQuantity(final int expected) {
        assertEquals(String.valueOf(expected),
                getNodeFinder().lookup("#goodsTransferAmount")
                        .queryAs(Label.class).getText(),
                "Unexpected chosen quantity");
    }

    public void verifyConfirmation(final String expectedTitle,
            final String expectedQuestion) {
        assertEquals(expectedTitle, getNodeFinder().lookup("#jettisonTitle")
                .queryAs(Label.class).getText());
        assertEquals(expectedQuestion, getNodeFinder()
                .lookup("#jettisonQuestion").queryAs(Label.class).getText());
    }

    public void verifyDialogsClosed() {
        assertTrue(
                getNodeFinder().lookup("#jettisonQuestion").tryQuery().isEmpty()
                        && getNodeFinder().lookup("#jettisonHoldQuestion")
                                .tryQuery().isEmpty()
                        && getNodeFinder().lookup("#goodsTransferSlider")
                                .tryQuery().isEmpty(),
                "Jettison dialogs must be closed");
    }

    public void verifyMessage(final String expected) {
        assertTrue(
                getNodeFinder()
                        .lookup((Node node) -> node instanceof Label
                                && expected.equals(((Label) node).getText()))
                        .tryQuery().isPresent(),
                "Missing message: " + expected);
    }

    /** Goods of one hold of a ship, checked against the live model. */
    public void verifyHold(final int shipId, final int slot,
            final Goods expected) {
        final UnitWithCargo ship = (UnitWithCargo) getModel()
                .getUnitById(shipId);
        assertEquals(expected,
                ship.getCargo().getSlotByIndex(slot).getGoods().orElseThrow(),
                "Unexpected goods in hold " + (slot + 1) + " of ship "
                        + shipId);
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

    public void verifyGold(final int shipId, final int expected) {
        assertEquals(expected,
                getModel().getUnitById(shipId).getOwner().getGold(),
                "Unexpected treasury");
    }

    /** The ship keeps its voyage: direction, position and remaining turns. */
    public void verifyTransit(final int shipId, final boolean toEurope,
            final int position, final int remaining) {
        final PlaceHighSea voyage = (PlaceHighSea) getModel()
                .getUnitById(shipId).getPlace();
        assertEquals(toEurope, voyage.isTravelToEurope());
        assertEquals(position, voyage.getPosition());
        assertEquals(remaining, voyage.getRemainigTurns());
    }

    public void verifyShipAtMapSquare(final int shipId, final Location at) {
        assertEquals(at, getModel().getUnitById(shipId).getLocation(),
                "Ship must stay on its square");
    }

    /** The map selection holds the expected unit, or none. */
    public void verifySelectedUnit(final Optional<Integer> expectedId) {
        final SelectedUnitManager selection = getContext()
                .getClassFromGuice(SelectedUnitManager.class);
        assertEquals(expectedId, selection.getSelectedUnit().map(Unit::getId),
                "Unexpected selected unit");
    }
}
