package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitActionSentryPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitActionType;

import com.google.gson.Gson;

class SentryOrderTest {
    private static final Location WATER = Location.of(49, 15);
    private static final Location SHORE = Location.of(48, 15);

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private PlaceMapPo at(final Location location) {
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(location);
        place.setOrientation(Direction.west);
        return place;
    }

    private GameModelPo mapScenario() {
        final GameModelPo data = scenario();
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        ship.setPlaceHighSeas(null);
        ship.setDepartureLocation(null);
        ship.setDepartureDirection(null);
        ship.setPlaceMap(at(WATER));
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 15 || unit.getId() == 16)
                .forEach(unit -> unit.setAvailableMoves(2));
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(WATER, TerrainType.OCEAN);
        terrain.put(SHORE, TerrainType.GRASSLAND);
        terrain.put(Location.of(49, 16), TerrainType.OCEAN);
        terrain.put(Location.of(49, 17), TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        return data;
    }

    @Test
    void orderPreservesMovesAndRestoresWithoutSelectingOrReplaying(
            @TempDir final Path directory) {
        final GameModelPo data = mapScenario();
        data.getModel().getUnits().stream().filter(unit -> unit.getId() != 16)
                .forEach(unit -> unit.setAvailableMoves(0));
        final Model model = load(data);
        final Unit unit = model.getUnitById(16);
        unit.placeToLocation(SHORE);
        final int moves = unit.getActionPoints();
        assertSame(unit, model.getFirstSelectableUnit().orElseThrow());
        assertTrue(model.canSetSentry(unit));
        model.setSentry(unit);
        assertEquals(moves, unit.getActionPoints());
        assertEquals(UnitActionType.sentry, unit.getAction().getType());
        assertFalse(model.canSetSentry(unit));
        assertThrows(IllegalStateException.class, () -> model.setSentry(unit));
        assertTrue(model.getFirstSelectableUnit().isEmpty());
        assertSame(unit, model.getFirstSelectableUnitAt(SHORE).orElseThrow());
        data.setModel(model.save());
        final Path file = directory.resolve("sentry.microcol");
        final GameModelPoDao dao = new GameModelPoDao();
        dao.saveToFile(file.toString(), data);
        final Model restored = Model.make(
                dao.loadFromFile(file.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit waiting = restored.getUnitById(16);
        assertEquals(UnitActionType.sentry, waiting.getAction().getType());
        assertEquals(moves, waiting.getActionPoints());
        assertTrue(waiting.isAtPlaceLocation());
        assertTrue(restored.getFirstSelectableUnit().isEmpty());
        restored.activateSentry(waiting);
        assertEquals(moves, waiting.getActionPoints());
        assertEquals(UnitActionType.noAction, waiting.getAction().getType());
        assertSame(waiting, restored.getFirstSelectableUnit().orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> restored.activateSentry(waiting));
    }

    @Test
    void mapDepartureBoardsOnlyOrderedPassengersAndKeepsEquipmentAndCost() {
        final GameModelPo data = mapScenario();
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 16)
                .findFirst().orElseThrow().setTools(80);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        final Unit passenger = model.getUnitById(16);
        passenger.placeToLocation(SHORE);
        final int before = passenger.getActionPoints();
        final int shipMoves = ship.getActionPoints();
        model.setSentry(passenger);
        ship.moveOneStep(Location.of(49, 16));
        assertTrue(passenger.isAtCargoSlot());
        assertEquals(before - 1, passenger.getActionPoints());
        assertEquals(shipMoves - 1, ship.getActionPoints());
        assertEquals(UnitActionType.noAction, passenger.getAction().getType());
        assertEquals(80, ((UnitFreeColonist) passenger).getTools());
        assertSame(passenger, passenger.getPlaceCargoSlot().getCargoSlot()
                .getUnit().orElseThrow());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.getUnitById(16).isAtCargoSlot());
        assertEquals(UnitActionType.noAction,
                restored.getUnitById(16).getAction().getType());
        assertEquals(80,
                ((UnitFreeColonist) restored.getUnitById(16)).getTools());
    }

    @Test
    void rejectedDeparturePreservesAllOrdersAndCargo() {
        final Model model = load(mapScenario());
        final Ship ship = (Ship) model.getUnitById(3);
        final Unit passenger = model.getUnitById(16);
        passenger.placeToLocation(SHORE);
        model.setSentry(passenger);
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalArgumentException.class,
                () -> ship.moveOneStep(SHORE));
        assertEquals(before, new Gson().toJson(model.save()));
        assertEquals(UnitActionType.sentry, passenger.getAction().getType());
    }

    @Test
    void exhaustedAdjacentSentryRemainsAshoreAndOwnerChecksAreStrict() {
        final GameModelPo data = mapScenario();
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 16)
                .findFirst().orElseThrow().setAvailableMoves(0);
        final Model model = load(data);
        final Unit passenger = model.getUnitById(16);
        passenger.placeToLocation(SHORE);
        model.setSentry(passenger);
        final Ship ship = (Ship) model.getUnitById(3);
        ship.moveOneStep(Location.of(49, 16));
        assertTrue(passenger.isAtPlaceLocation());
        assertEquals(UnitActionType.sentry, passenger.getAction().getType());
        assertEquals(0, passenger.getActionPoints());

        final Unit foreign = load(mapScenario()).getUnitById(16);
        assertFalse(model.canSetSentry(foreign));
        assertThrows(IllegalStateException.class,
                () -> model.setSentry(foreign));
        model.stop();
        assertFalse(model.canActivateSentry(passenger));
        assertThrows(IllegalStateException.class,
                () -> model.activateSentry(passenger));
    }

    @Test
    void europeDepartureBoardsPierSentryWithoutSpendingMoves() {
        final Model model = load(mapScenario());
        final Ship ship = (Ship) model.getUnitById(3);
        final Unit passenger = model.getUnitById(16);
        ship.placeToEuropePort(model.getEurope().getPort());
        passenger.placeToEuropePortPier();
        final int moves = passenger.getActionPoints();
        model.setSentry(passenger);
        ship.placeToHighSeas(false);
        assertTrue(passenger.isAtCargoSlot());
        assertEquals(moves, passenger.getActionPoints());
        assertEquals(UnitActionType.noAction, passenger.getAction().getType());
        assertTrue(ship.isAtHighSea());
    }

    @Test
    void invalidEuropeDepartureCannotBoardOrClearPierOrder() {
        final GameModelPo data = mapScenario();
        final UnitPo shipPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        shipPo.setPlaceMap(null);
        shipPo.setPlaceEuropePort(new PlaceEuropePortPo());
        shipPo.setAction(new UnitActionSentryPo());
        final UnitPo passengerPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 16).findFirst().orElseThrow();
        passengerPo.setPlaceCargoSlot(null);
        final PlaceEuropePortPo pier = new PlaceEuropePortPo();
        pier.setOnPier(true);
        passengerPo.setPlaceEuropePort(pier);
        shipPo.getCargo().getSlots().get(0).setUnitId(null);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        final Unit passenger = model.getUnitById(16);
        model.setSentry(passenger);
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> ship.placeToHighSeas(false));
        assertEquals(before, new Gson().toJson(model.save()));
        assertTrue(passenger.isAtEuropePier());
        assertEquals(UnitActionType.sentry, passenger.getAction().getType());
    }

    @Test
    void stableIdentityOrderUsesOnlyFreeHoldsAndLeavesExcessWaiting() {
        final GameModelPo data = mapScenario();
        final UnitPo shipPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        for (int id : new int[] { 15, 16 }) {
            final UnitPo passengerPo = data.getModel().getUnits().stream()
                    .filter(unit -> unit.getId() == id).findFirst()
                    .orElseThrow();
            passengerPo.setPlaceCargoSlot(null);
            passengerPo.setPlaceMap(at(SHORE));
            shipPo.getCargo().getSlots().stream()
                    .filter(slot -> Integer.valueOf(id)
                            .equals(slot.getUnitId()))
                    .findFirst().orElseThrow().setUnitId(null);
        }
        shipPo.getCargo().getSlots().get(0).setGoodsType(GoodsType.ORE);
        shipPo.getCargo().getSlots().get(0).setAmount(1);
        shipPo.getCargo().getSlots().get(4).setGoodsType(GoodsType.ORE);
        shipPo.getCargo().getSlots().get(4).setAmount(1);
        final CargoSlotPo finalHold = new CargoSlotPo();
        finalHold.setGoodsType(GoodsType.ORE);
        finalHold.setAmount(1);
        shipPo.getCargo().getSlots().add(finalHold);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        assertEquals(1, ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isEmpty).count());
        model.setSentry(model.getUnitById(16));
        model.setSentry(model.getUnitById(15));
        ship.moveOneStep(Location.of(49, 16));
        assertTrue(model.getUnitById(15).isAtCargoSlot());
        assertEquals(UnitActionType.noAction,
                model.getUnitById(15).getAction().getType());
        assertTrue(model.getUnitById(16).isAtPlaceLocation());
        assertEquals(UnitActionType.sentry,
                model.getUnitById(16).getAction().getType());
    }

    @Test
    void onlyVisibleAdjacentForeignMovementWakesMapSentry() {
        final GameModelPo data = mapScenario();
        data.getModel().getUnits()
                .removeIf(unit -> unit.getId() == 3 || unit.getId() == 13
                        || unit.getId() == 14 || unit.getId() == 15);
        final UnitPo waiting = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 16).findFirst().orElseThrow();
        waiting.setPlaceCargoSlot(null);
        waiting.setPlaceMap(at(SHORE));
        waiting.setAction(new UnitActionSentryPo());
        final UnitPo foreign = new UnitPo();
        foreign.setId(100);
        foreign.setType(UnitType.CARAVEL);
        foreign.setOwnerId("Dutch's King");
        foreign.setAvailableMoves(4);
        foreign.setAction(new UnitActionNoActionPo());
        foreign.setPlaceMap(at(Location.of(49, 16)));
        data.getModel().getUnits().add(foreign);
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        final Model model = load(data);
        final Unit sentry = model.getUnitById(16);
        final int moves = sentry.getActionPoints();
        model.getUnitById(100).moveOneStep(Location.of(49, 17));
        assertEquals(UnitActionType.sentry, sentry.getAction().getType());
        model.getUnitById(100).moveOneStep(Location.of(49, 16));
        assertEquals(UnitActionType.noAction, sentry.getAction().getType());
        assertEquals(moves, sentry.getActionPoints());
    }
}
