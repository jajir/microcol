package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

/** Passenger choices and observations through the ordinary game controls. */
public final class PassengerTransferPage extends AbstractScreen {
    public PassengerTransferPage(TestContext context) {
        super(context);
    }

    public void choose(int unitId) {
        getRobot().clickOn("#passenger-choice-" + unitId);
    }

    public void confirm() {
        clickOnButtonWithId("passenger-confirm");
    }

    public void cancel() {
        clickOnButtonWithId("passenger-cancel");
    }

    public void escape() {
        getRobot().press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeRejection() {
        clickOnButtonWithId("buttonOk");
    }

    public void disembarkInPort(int unitId, int slotIndex, boolean europe)
            throws Exception {
        getRobot()
                .drag(PageComponentPanelDock.of(getContext()).getListOfCrates()
                        .get(slotIndex), MouseButton.PRIMARY)
                .dropTo(europe ? ".panel-port-pier" : ".outside");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> europe ? getModel().getUnitById(unitId).isAtEuropePier()
                        : getModel().getUnitById(unitId).isAtPlaceLocation());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dragPassengerToSlot(int unitId, int slotIndex, boolean europe)
            throws InterruptedException {
        final Node source = getNodeFinder().lookup(
                (europe ? "#europe-pier-unit-" : "#colonyUnit-") + unitId)
                .query();
        final CountDownLatch finished = new CountDownLatch(1);
        final EventHandler<DragEvent> done = event -> finished.countDown();
        getRobot().interact(
                () -> source.addEventHandler(DragEvent.DRAG_DONE, done));
        try {
            getRobot().drag(source, MouseButton.PRIMARY)
                    .dropTo(PageComponentPanelDock.of(getContext())
                            .getListOfCrates().get(slotIndex));
            // A rejected native drop can outlive the robot and FX event queue.
            if (!finished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "The native passenger drag must finish");
            }
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            getRobot().interact(
                    () -> source.removeEventHandler(DragEvent.DRAG_DONE, done));
        }
    }

    public void embarkInPort(int unitId, int slotIndex, boolean europe)
            throws Exception {
        dragPassengerToSlot(unitId, slotIndex, europe);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).isAtCargoSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyOnPier(int unitId, int moves, int tools) {
        UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        assertTrue(unit.isAtEuropePier());
        assertEquals(moves, unit.getActionPoints());
        assertEquals(tools, unit.getTools());
        assertFalse(((UnitWithCargo) getModel().getUnitById(3)).getCargo()
                .getSlots().stream()
                .anyMatch(slot -> slot.getUnit().orElse(null) == unit));
    }

    public void verifyChoiceCount(int expectedCount) {
        assertEquals(expectedCount,
                getNodeFinder().lookup(".radio-button").queryAll().size());
    }

    public void verifyChoice(int unitId, boolean selected) {
        RadioButton choice = getNodeFinder()
                .lookup("#passenger-choice-" + unitId)
                .queryAs(RadioButton.class);
        assertTrue(choice.isVisible());
        assertEquals(selected, choice.isSelected());
        assertTrue(isCssIdVisible("passenger-confirm"));
    }

    public void verifyAboard(int unitId, int slotIndex, int moves, int tools) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(3);
        UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        CargoSlot slot = ship.getCargo().getSlotByIndex(slotIndex);
        assertTrue(unit.isAtCargoSlot());
        assertSame(unit, slot.getUnit().orElseThrow());
        assertSame(slot, unit.getPlaceCargoSlot().getCargoSlot());
        assertEquals(moves, unit.getActionPoints());
        assertEquals(tools, unit.getTools());
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
    }

    public void verifyAshore(int unitId, Location location, int moves,
            int tools) {
        UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(location, unit.getLocation());
        assertEquals(moves, unit.getActionPoints());
        assertEquals(tools, unit.getTools());
        assertFalse(((UnitWithCargo) getModel().getUnitById(3)).getCargo()
                .getSlots().stream()
                .anyMatch(slot -> slot.getUnit().orElse(null) == unit));
    }

    public void verifyShip(Location location, int moves) {
        Unit ship = getModel().getUnitById(3);
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(location, ship.getLocation());
        assertEquals(moves, ship.getActionPoints());
    }

    public void verifyFullShip(int shipId) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(shipId);
        assertTrue(ship.getCargo().getSlots().stream()
                .noneMatch(CargoSlot::isEmpty));
    }

    public void verifyCargoSlotEmpty(int shipId, int slotIndex) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(shipId);
        assertTrue(ship.getCargo().getSlotByIndex(slotIndex).isEmpty());
    }

    public void verifyCargoGoods(int shipId, int slotIndex, Goods expected) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(shipId);
        assertEquals(expected, ship.getCargo().getSlotByIndex(slotIndex)
                .getGoods().orElseThrow());
    }

    public void verifyForeignShip(int id, String owner, Location location) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(id);
        assertEquals(owner, ship.getOwner().getName());
        assertEquals(location, ship.getLocation());
        assertTrue(ship.getCargo().getSlots().stream()
                .allMatch(CargoSlot::isEmpty));
    }

    public void verifyMovementRejected() {
        assertTrue(isCssIdVisible("buttonOk"));
        assertTrue(getNodeFinder().lookup("I't not possible to move unit here.")
                .queryAs(javafx.scene.control.Label.class).isVisible());
    }

    public void verifyRejection() {
        assertTrue(isCssIdVisible("buttonOk"));
        assertTrue(getNodeFinder().lookup(
                "This passenger cannot transfer now. Check the ship, destination, free cargo space and remaining movement.")
                .queryAs(javafx.scene.control.Label.class).isVisible());
    }
}
