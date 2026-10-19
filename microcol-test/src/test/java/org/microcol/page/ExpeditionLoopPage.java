package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.Ship;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;

import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

/**
 * Ordinary player actions and read-only observations for the integrated
 * four-nation expedition journey. Identities are read from the live model
 * because unit identifiers and start squares differ for every nation.
 */
public final class ExpeditionLoopPage extends AbstractScreen {
    private static final int MAX_TAB_PRESSES = 8;

    private final EuropeanNation nation;

    private String remembered;

    public ExpeditionLoopPage(final TestContext context,
            final EuropeanNation nation) {
        super(context);
        this.nation = com.google.common.base.Preconditions.checkNotNull(nation);
    }

    private Player player() {
        return getModel().getPlayerByName(nation.getPlayerName());
    }

    private Ship ship() {
        return (Ship) player().getAllUnits().stream().filter(Unit::isShip)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "The expedition has no ship"));
    }

    private Colony colony(final String name) {
        return getModel().getColonyByName(name).orElseThrow(
                () -> new IllegalStateException("No colony named " + name));
    }

    private int selectedUnitIdOrMinusOne() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit().map(Unit::getId).orElse(-1);
    }

    /** @return the identifier of the expedition ship */
    public int shipId() {
        return ship().getId();
    }

    /** @return the ship passengers' identifiers, lowest first */
    public List<Integer> passengerIds() {
        return player().getAllUnits().stream().filter(unit -> !unit.isShip())
                .map(Unit::getId).sorted().collect(Collectors.toList());
    }

    /** @return the square currently occupied by the ship on the map */
    public Location shipLocation() {
        return ship().getLocation();
    }

    /** @return the identifier allocated to the next founded colony */
    public int nextColonyId() {
        return getModel().save().getNextColonyId();
    }

    /** @return the current number of human and rival colonies */
    public int colonyCount() {
        return getModel().getColonies().size();
    }

    /** @return completed turns of the running calendar */
    public int playedTurns() {
        return getModel().getCalendar().getNumberOfPlayedTurns();
    }

    /** @return the weather sampled for the ship's current Atlantic voyage */
    public int voyageWeather() {
        return ((PlaceHighSea) ship().getPlace()).getWeather();
    }

    /** @return the unit currently selected on the map */
    public Unit selectedUnit() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit().orElseThrow(
                        () -> new IllegalStateException("No unit is selected"));
    }

    /**
     * Select the ship with the Tab key a player uses to cycle through units. A
     * fresh game already marks the ship's tile as selected, so a map click on
     * it changes nothing; only when Tab leaves another unit selected is the
     * tile clicked. A ship that cannot be selected is a blocked route.
     */
    public void selectShip(final GamePage game) throws Exception {
        final Ship ship = ship();
        if (selectedUnitIdOrMinusOne() == ship.getId()) {
            return;
        }
        if (ship.isAtPlaceLocation()
                && getModel().getColonyAt(ship.getLocation()).isEmpty()) {
            game.panToTile(ship.getLocation());
            game.selectTile(ship.getLocation());
        }
        // A fresh game's already selected tile may not emit another click
        // event.
        for (int press = 0; press < MAX_TAB_PRESSES
                && selectedUnitIdOrMinusOne() != ship.getId(); press++) {
            game.pressTab();
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (selectedUnitIdOrMinusOne() != ship.getId()) {
            throw new IllegalStateException(
                    "The expedition ship could not be selected");
        }
    }

    /** Drag the selected ship over the map to a square within one turn. */
    public void sailTo(final GamePage game, final Location target)
            throws Exception {
        selectShip(game);
        final Location from = shipLocation();
        game.panToTile(from);
        game.panToTile(target);
        game.moveMouseAtLocation(from);
        game.dragMouseAtLocation(target);
    }

    /** Land one passenger with the ordinary passenger choice dialog. */
    public void disembark(final GamePage game, final Location land,
            final int passengerId, final int expectedUnitsAtLand)
            throws Exception {
        selectShip(game);
        final Location from = shipLocation();
        game.panToTile(from);
        game.panToTile(land);
        game.moveMouseAtLocation(from);
        game.dragMouseAtLocation(land);
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.choose(passengerId);
        passengers.confirm();
        game.waitForNumberOfUnitsAt(land, expectedUnitsAtLand);
    }

    /** Make a landed colonist the selected unit before founding. */
    public void selectColonistAt(final GamePage game, final Location site)
            throws Exception {
        game.panToTile(site);
        final boolean selectedOnSite = selectedUnitIdOrMinusOne() >= 0
                && selectedUnit().isAtPlaceLocation()
                && !selectedUnit().isShip()
                && site.equals(selectedUnit().getLocation());
        if (!selectedOnSite) {
            game.selectTile(site);
        }
        if (selectedUnitIdOrMinusOne() < 0 || selectedUnit().isShip()
                || !selectedUnit().isAtPlaceLocation()
                || !site.equals(selectedUnit().getLocation())) {
            throw new IllegalStateException(
                    "No landed colonist is selected at " + site);
        }
    }

    /** Open the work choice of a colonist who already works on a field. */
    public void openFieldJobChoice(final ColonyScreen colonyScreen,
            final int unitId) {
        colonyScreen.openCropMenu(unitId);
        getRobot().clickOn("#chooseWork");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Wait until the ship hold shows the dragged goods. */
    public void waitUntilShipCarries(final int slot, final GoodsType type)
            throws TimeoutException {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> ship().getCargo().getSlotByIndex(slot).getGoods()
                        .map(goods -> goods.getType() == type).orElse(false));
    }

    /** Wait until a sale has emptied one ship hold. */
    public void waitUntilShipSlotIsEmpty(final int slot)
            throws TimeoutException {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> ship().getCargo().getSlotByIndex(slot).isEmpty());
    }

    /** @return the identifier of the colonist just recruited onto the pier */
    public int waitForRecruitOnPier() throws TimeoutException {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> player()
                .getAllUnits().stream().anyMatch(Unit::isAtEuropePier));
        return player().getAllUnits().stream().filter(Unit::isAtEuropePier)
                .map(Unit::getId).findFirst().orElseThrow();
    }

    /** Drop the selected port ship on the departure panel of Europe. */
    public void sendShipBack() throws Exception {
        final Parent panel = getNodeFinder().lookup(".to-new-world")
                .queryAs(Parent.class);
        final HBox target = (HBox) panel.getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof HBox
                        && node.getOnDragDropped() != null)
                .findFirst().orElseThrow();
        getRobot().drag(
                PageComponentPanelDock.of(getContext()).getUnitsInPort().get(0),
                MouseButton.PRIMARY).dropTo(target);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> ship().isAtHighSea());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Remember the complete ledger before leaving the game for the menu. */
    public void rememberLedger() {
        remembered = ledger();
    }

    private String ledger() {
        final StringBuilder out = new StringBuilder();
        out.append(new Gson().toJson(getModel().save().getCalendar()))
                .append('\n');
        out.append("gold=").append(player().getGold()).append('\n');
        getModel().getAllUnits().stream()
                .sorted(Comparator.comparingInt(Unit::getId))
                .forEach(unit -> out.append(describe(unit)).append('\n'));
        getModel().getColonies().stream()
                .sorted(Comparator.comparingInt(Colony::getId))
                .forEach(colony -> {
                    out.append("colony ").append(colony.getId()).append(' ')
                            .append(colony.getName()).append(' ')
                            .append(colony.getLocation()).append(' ')
                            .append(colony.getOwner().getName());
                    GoodsType.GOOD_TYPES
                            .forEach(type -> out.append(' ').append(type.name())
                                    .append('=').append(colony.getWarehouse()
                                            .getGoods(type).getAmount()));
                    out.append('\n');
                });
        return out.toString();
    }

    private String describe(final Unit unit) {
        final StringBuilder text = new StringBuilder().append(unit.getId())
                .append(' ').append(unit.getType().name()).append(' ')
                .append(unit.getOwner().getName()).append(' ');
        if (unit.isAtPlaceLocation()) {
            text.append("map ").append(unit.getLocation());
        } else if (unit.isAtCargoSlot()) {
            text.append("cargo ")
                    .append(unit.getPlaceCargoSlot().getOwnerUnit().getId());
        } else if (unit.isAtEuropePier()) {
            text.append("pier");
        } else if (unit.isAtEuropePort()) {
            text.append("port");
        } else if (unit.isAtHighSea()) {
            final PlaceHighSea voyage = (PlaceHighSea) unit.getPlace();
            text.append("sea ").append(voyage.isTravelToEurope()).append(' ')
                    .append(voyage.getWeather()).append(' ')
                    .append(voyage.getOriginalTurns()).append(' ')
                    .append(voyage.getRemainigTurns());
        } else {
            text.append("colony");
        }
        if (unit instanceof org.microcol.model.unit.UnitWithCargo) {
            ((org.microcol.model.unit.UnitWithCargo) unit).getCargo().getSlots()
                    .forEach(
                            slot -> text.append(" slot:")
                                    .append(slot.getGoods()
                                            .map(goods -> goods.getType().name()
                                                    + goods.getAmount())
                                            .orElse("-")));
        }
        return text.toString();
    }

    public void verifyLedgerUnchanged() {
        assertEquals(remembered, ledger(),
                "Save and Continue must keep stocks, ownership and date");
    }

    public void verifyShipAt(final Location expected) {
        final Ship ship = ship();
        assertTrue(ship.isAtPlaceLocation(),
                "The ship must be on the map, not at sea or in Europe");
        assertEquals(expected, ship.getLocation(), "Unexpected ship square");
    }

    public void verifyShipType(final UnitType expected) {
        assertSame(expected, ship().getType());
        assertEquals(expected.getCargoCapacity(),
                ship().getCargo().getSlots().size());
    }

    public void verifyShipInEurope(final boolean expected) {
        assertEquals(expected, ship().isAtEuropePort());
    }

    public void verifyPlayedTurns(final int expected) {
        assertEquals(expected, playedTurns(), "Unexpected completed turns");
    }

    /** A ship without movement cannot start a map drag, so this is explicit. */
    public void verifyShipMovesLeft(final int expected) {
        assertEquals(expected, ship().getActionPoints(),
                "Unexpected movement left on the ship");
    }

    public void verifyNoTaxDecisionPending() {
        assertFalse(getModel().isAwaitingTaxDecision(),
                "The first tax demand must not interrupt the journey");
    }

    public void verifyGold(final int expected) {
        assertEquals(expected, player().getGold(), "Unexpected treasury");
    }

    public void verifyTerrainExplored(final Location location,
            final boolean expected) {
        assertEquals(expected, player().isVisible(location),
                "Unexpected exploration of " + location);
    }

    public void verifyPassengerIds(final List<Integer> expected) {
        assertEquals(expected, passengerIds());
        assertEquals(2, expected.size());
        assertTrue(expected.stream().allMatch(id -> getModel().getUnitById(id)
                .getPlace() instanceof org.microcol.model.PlaceCargoSlot));
    }

    public void verifyColonyStock(final String colonyName,
            final Goods expected) {
        assertEquals(expected.getAmount(),
                colony(colonyName).getWarehouse().getGoods(expected.getType())
                        .getAmount(),
                "Unexpected stock of " + expected.getType());
    }

    public void verifyWorkerOutput(final String colonyName, final int unitId,
            final Goods expected) {
        final ColonyField field = colony(colonyName).getColonyFields().stream()
                .filter(f -> f.getUnit().map(Unit::getId).orElse(-1) == unitId)
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Unit " + unitId + " works on no field"));
        assertEquals(expected, field.getProduction().orElseThrow());
    }

    public void verifyWorkerIndoors(final String colonyName, final int unitId,
            final ConstructionType building) {
        final boolean working = colony(colonyName)
                .getConstructionByType(building).getConstructionSlots().stream()
                .anyMatch(slot -> !slot.isEmpty()
                        && slot.getUnit().getId() == unitId);
        assertTrue(working, "Unit " + unitId + " must work in " + building);
        assertFalse(colony(colonyName).getColonyFields().stream().anyMatch(
                f -> f.getUnit().map(Unit::getId).orElse(-1) == unitId));
    }

    public void verifyShipCargo(final int slot, final Goods expected) {
        assertEquals(expected,
                ship().getCargo().getSlotByIndex(slot).getGoods()
                        .orElse(Goods.of(expected.getType())),
                "Unexpected goods in the ship hold");
    }

    public void verifyPassengerAboard(final int unitId) {
        final Unit unit = getModel().getUnitById(unitId);
        assertTrue(unit.isAtCargoSlot());
        assertEquals(ship().getId(),
                unit.getPlaceCargoSlot().getOwnerUnit().getId());
        assertSame(player(), unit.getOwner());
    }

    public void verifyUnitAt(final int unitId, final Location expected) {
        final Unit unit = getModel().getUnitById(unitId);
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(expected, unit.getLocation());
        assertSame(player(), unit.getOwner());
    }

    /**
     * Check one Atlantic crossing against the voyage table: two turns, plus the
     * sampled weather, plus one for the westward return, limited to 1..4.
     *
     * @param toEurope whether the ship sails to Europe
     * @return the expected number of turns, equal to the sampled voyage
     */
    public int verifyVoyage(final boolean toEurope) {
        final Ship ship = ship();
        assertTrue(ship.getPlace() instanceof PlaceHighSea);
        final PlaceHighSea voyage = (PlaceHighSea) ship.getPlace();
        final int weather = voyage.getWeather();
        assertTrue(weather >= -1 && weather <= 1);
        assertEquals(toEurope, voyage.isTravelToEurope());
        assertEquals(Direction.east,
                ship.getDepartureDirection().orElseThrow());
        final int expected = Math.max(1,
                Math.min(4, 2 + weather + (toEurope ? 0 : 1)
                        - (ship.getType().getSpeed() >= 6 ? 1 : 0)));
        assertEquals(expected, voyage.getOriginalTurns());
        assertEquals(expected, voyage.getRemainigTurns());
        return expected;
    }

    public void verifyVoyageProgress(final int weather, final int original,
            final int remaining, final boolean toEurope) {
        final PlaceHighSea voyage = (PlaceHighSea) ship().getPlace();
        assertEquals(weather, voyage.getWeather());
        assertEquals(original, voyage.getOriginalTurns());
        assertEquals(remaining, voyage.getRemainigTurns());
        assertEquals(toEurope, voyage.isTravelToEurope());
    }
}
