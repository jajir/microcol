package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitWithCargo;

import javafx.scene.control.ToggleButton;

/** Wagon gestures and separate verification of carriers and cargo. */
public final class WagonUnloadPage extends AbstractScreen {

    public WagonUnloadPage(final TestContext context) {
        super(context);
    }

    public void verifyCarriers(final List<Integer> expectedIds,
            final int expectedSelectedId) {
        final List<ToggleButton> buttons = PageComponentPanelDock
                .of(getContext()).getUnitsInPort();
        assertEquals(expectedIds,
                buttons.stream()
                        .map(button -> ((Unit) button.getUserData()).getId())
                        .collect(Collectors.toList()));
        assertEquals(List.of(expectedSelectedId),
                buttons.stream().filter(ToggleButton::isSelected)
                        .map(button -> ((Unit) button.getUserData()).getId())
                        .collect(Collectors.toList()));
    }

    public void verifyWagon(final int id, final String expectedOwner,
            final Location expectedLocation, final int expectedOre) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(UnitType.WAGON, unit.getType());
        assertEquals(expectedOwner, unit.getOwner().getName());
        assertEquals(expectedLocation, unit.getLocation());
        final UnitWithCargo wagon = (UnitWithCargo) unit;
        assertEquals(2, wagon.getCargo().getSlots().size());
        final ShipWrapper cargo = new ShipWrapper(wagon);
        cargo.verifyCargoGoods(0, Goods.of(GoodsType.ORE, expectedOre));
        cargo.verifyCargoSlotEmpty(1);
    }

    public void dragWagonIconToEmptyCargoSlot(final int carrierIndex,
            final int cargoSlot) {
        final PageComponentPanelDock dock = PageComponentPanelDock
                .of(getContext());
        getRobot()
                .drag(dock.getUnitsInPort().get(carrierIndex),
                        javafx.scene.input.MouseButton.PRIMARY)
                .dropTo(dock.getListOfCrates().get(cargoSlot))
                .release(javafx.scene.input.MouseButton.PRIMARY);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyWagonNotAboard(final int shipId, final int wagonId) {
        final UnitWithCargo ship = (UnitWithCargo) getModel()
                .getUnitById(shipId);
        final Unit wagon = getModel().getUnitById(wagonId);
        assertTrue(ship.getCargo().getSlots().stream()
                .noneMatch(slot -> slot.getUnit().orElse(null) == wagon));
        assertTrue(ship.getCargo().getSlotByIndex(2).isEmpty());
    }

    public void verifyColonistOutside(final int id, final String owner,
            final Location location) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(UnitType.COLONIST, unit.getType());
        assertEquals(owner, unit.getOwner().getName());
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(location, unit.getLocation());
    }
}
