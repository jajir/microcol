package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import org.microcol.model.*;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

/** Read-only voyage observations and the ordinary Europe return gesture. */
public final class AtlanticAuditPage extends AbstractScreen {
    public AtlanticAuditPage(final TestContext context) {
        super(context);
    }

    public void verifyVoyage(final int turns, final boolean toEurope) {
        final Unit ship = getModel().getUnitById(3);
        assertTrue(ship.getPlace() instanceof PlaceHighSea);
        final PlaceHighSea place = (PlaceHighSea) ship.getPlace();
        assertEquals(turns, place.getRemainigTurns());
        assertEquals(toEurope, place.isTravelToEurope());
    }

    public void verifyCargo() {
        final UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(3);
        new ShipWrapper(ship).verifyCargoGoods(0, Goods.of(GoodsType.ORE, 7));
        final UnitFreeColonist passenger = (UnitFreeColonist) getModel()
                .getUnitById(4);
        assertTrue(passenger.getPlace() instanceof PlaceCargoSlot);
        assertSame(ship.getCargo().getSlotByIndex(1),
                passenger.getPlaceCargoSlot().getCargoSlot());
        assertEquals(3, passenger.getPlaceCargoSlot().getOwnerUnit().getId());
        assertEquals(80, passenger.getTools());
        assertFalse(passenger.isHoldingGuns());
    }

    public void verifyInEurope() {
        assertTrue(getModel().getUnitById(3).isAtEuropePort());
    }

    public void verifyMovement(final int expected) {
        assertEquals(expected, getModel().getUnitById(3).getActionPoints());
    }

    public void verifyVoyageText(final String expected) {
        assertTrue(isCssIdVisible("voyage-remaining-3"));
        assertEquals(expected, getLabeledById("voyage-remaining-3").getText());
        final javafx.scene.text.Text rendered = (javafx.scene.text.Text) getLabeledById(
                "voyage-remaining-3").lookup(".text");
        assertEquals(expected, rendered.getText(),
                "The visible voyage reason must not be truncated");
    }

    public void sendShipBack() throws Exception {
        attemptSendShipBack();
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(3).isAtHighSea());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void attemptSendShipBack() {
        final Parent panel = getNodeFinder().lookup(".to-new-world")
                .queryAs(Parent.class);
        final HBox target = (HBox) panel.getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof HBox).findFirst().get();
        getRobot().drag(
                PageComponentPanelDock.of(getContext()).getUnitsInPort().get(0),
                MouseButton.PRIMARY).dropTo(target);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDepartureRejection(final String expected) {
        assertEquals(expected, getNodeFinder().lookup(expected)
                .queryAs(javafx.scene.control.Label.class).getText());
        assertTrue(isCssIdVisible("buttonOk"));
    }

    public void closeRejection() {
        clickOnButtonWithId("buttonOk");
    }

    public void verifyReturnedTo(final Location departure) {
        final Unit ship = getModel().getUnitById(3);
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(departure, ship.getLocation());
    }
}
