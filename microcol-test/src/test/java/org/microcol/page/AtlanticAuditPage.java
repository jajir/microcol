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

    public void sendShipBack() throws Exception {
        final Parent panel = getNodeFinder().lookup(".to-new-world")
                .queryAs(Parent.class);
        final HBox target = (HBox) panel.getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof HBox).findFirst().get();
        getRobot().drag(
                PageComponentPanelDock.of(getContext()).getUnitsInPort().get(0),
                MouseButton.PRIMARY).dropTo(target);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(3).isAtHighSea());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyReturnedAwayFrom(final Location departure) {
        final Unit ship = getModel().getUnitById(3);
        assertTrue(ship.isAtPlaceLocation());
        assertNotEquals(departure, ship.getLocation());
    }
}
