package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;

/**
 * The home port of a nation that declared independence (R-0187): the voyage
 * lists, the pier menu and the observable state of seized people, ships and
 * goods, through the ordinary Europe controls.
 */
public final class HomeEuropePage extends AbstractScreen {

    public HomeEuropePage(final TestContext context) {
        super(context);
    }

    /** Open the order menu of a person waiting on the pier. */
    public void openPierMenu(final int unitId) {
        getRobot().clickOn("#europe-pier-unit-" + unitId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closePierMenu() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** One crossing, with its remaining time as the Europe list shows it. */
    public void verifyVoyage(final int shipId, final boolean toEurope,
            final int position, final int remaining,
            final String expectedLabel) {
        final PlaceHighSea voyage = (PlaceHighSea) getModel()
                .getUnitById(shipId).getPlace();
        assertEquals(toEurope, voyage.isTravelToEurope(),
                "Direction of " + shipId);
        assertEquals(position, voyage.getPosition(), "Position of " + shipId);
        assertEquals(remaining, voyage.getRemainigTurns(),
                "Remaining time of " + shipId);
        assertEquals(expectedLabel,
                getNodeFinder().lookup("#voyage-remaining-" + shipId)
                        .queryAs(Label.class).getText());
    }

    /** The reverse control of a crossing is available or closed. */
    public void verifyReverseControl(final int shipId,
            final boolean expectedAvailable) {
        assertEquals(expectedAvailable,
                !getNodeFinder().lookup("#voyage-reverse-" + shipId)
                        .queryAs(Button.class).isDisabled(),
                "Reverse control of " + shipId);
    }

    /** A crossing keeps its goods and passenger, whichever way it sails. */
    public void verifyCrossingCargo(final int shipId, final GoodsType goods,
            final int amount, final int passengerId) {
        final Ship ship = (Ship) getModel().getUnitById(shipId);
        assertEquals(Goods.of(goods, amount),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(passengerId, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
    }

    /** The ship is in the home port with the listed hold contents. */
    public void verifyShipInPort(final int shipId, final int passengerId,
            final GoodsType goods, final int amount) {
        final Ship ship = (Ship) getModel().getUnitById(shipId);
        assertTrue(ship.isAtEuropePort(), "Port of " + shipId);
        assertEquals(passengerId, ship.getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId());
        assertEquals(Goods.of(goods, amount),
                ship.getCargo().getSlotByIndex(1).getGoods().orElseThrow());
    }

    /** A reversed crossing is over: the ship stands on its source square. */
    public void verifyArrivedAtSource(final int shipId,
            final Location expectedSource, final GoodsType goods,
            final int amount, final int passengerId) {
        final Ship ship = (Ship) getModel().getUnitById(shipId);
        assertTrue(ship.isAtPlaceLocation(), "Map place of " + shipId);
        assertEquals(expectedSource, ship.getLocation());
        verifyCrossingCargo(shipId, goods, amount, passengerId);
    }

    public void verifyPersonOnPier(final int unitId) {
        assertTrue(getModel().getUnitById(unitId).isAtEuropePier(),
                "Pier of " + unitId);
    }

    /** Whether the Crown holds the listed units in the home port. */
    public void verifySeized(final boolean expectedSeized,
            final int... unitIds) {
        for (final int unitId : unitIds) {
            final Unit unit = getModel().getUnitById(unitId);
            assertEquals(expectedSeized, unit.isCrownSeized(),
                    "Seizure of " + unitId);
        }
    }

    /** The pier menu offers no order, equipment or ordination to the person. */
    public void verifyPierMenuClosed(final int unitId) {
        final List<MenuItem> items = showingMenuItems();
        for (final String id : List.of("europe-sentry-" + unitId,
                "europe-equipment-MUSKET", "europe-equipment-HORSE",
                "europe-equipment-TOOLS", "europe-ordain-" + unitId)) {
            assertTrue(item(items, id).isDisable(), "Pier action " + id);
        }
    }

    /** The pier menu still offers its orders to a free person. */
    public void verifyPierMenuOpen(final int unitId) {
        final List<MenuItem> items = showingMenuItems();
        assertFalse(item(items, "europe-sentry-" + unitId).isDisable(),
                "Sentry order of " + unitId);
        assertFalse(item(items, "europe-equipment-MUSKET").isDisable(),
                "Muskets for " + unitId);
    }

    private List<MenuItem> showingMenuItems() {
        WaitForAsyncUtils.waitForFxEvents();
        return Window.getWindows().stream()
                .filter(window -> window instanceof ContextMenu
                        && window.isShowing())
                .<List<MenuItem>>map(
                        window -> ((ContextMenu) window).getItems())
                .findFirst().orElse(List.of());
    }

    private static MenuItem item(final List<MenuItem> items, final String id) {
        return items.stream().filter(item -> id.equals(item.getId()))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "No pier menu item " + id));
    }
}
