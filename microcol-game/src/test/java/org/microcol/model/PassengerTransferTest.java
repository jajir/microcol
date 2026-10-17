package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

class PassengerTransferTest {
    private static final Location WATER = Location.of(49, 15);
    private static final Location SHORE = Location.of(48, 15);
    private static final Location PORT = Location.of(25, 14);
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();
    private GameModelPo data;
    private Model model;

    private void prepare(Location shipAt, int moves) {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        UnitPo ship = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 3).findFirst().orElseThrow();
        ship.setPlaceHighSeas(null);
        ship.setDepartureLocation(null);
        ship.setDepartureDirection(null);
        ship.setPlaceMap(at(shipAt));
        UnitPo passenger = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 16).findFirst().orElseThrow();
        passenger.setAvailableMoves(moves);
        passenger.setTools(80);
        ship.getCargo().getSlots().get(4).setGoodsType(GoodsType.ORE);
        ship.getCargo().getSlots().get(4).setAmount(7);
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(WATER, TerrainType.OCEAN);
        terrain.put(SHORE, TerrainType.GRASSLAND);
        terrain.put(Location.of(25, 13), TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private PlaceMapPo at(Location location) {
        PlaceMapPo at = new PlaceMapPo();
        at.setLocation(location);
        at.setOrientation(Direction.west);
        return at;
    }

    private Ship ship() {
        return (Ship) model.getUnitById(3);
    }

    private Unit passenger() {
        return model.getUnitById(16);
    }

    private CargoSlot slot() {
        return ship().getCargo().getSlotByIndex(0);
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private void unchangedOnRejection(Runnable action) {
        String before = snapshot();
        assertThrows(IllegalStateException.class, action::run);
        assertEquals(before, snapshot());
    }

    private void otherCargoUnchanged() {
        assertEquals(80, ((UnitFreeColonist) passenger()).getTools());
        assertEquals(15, ship().getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertEquals(14, ship().getCargo().getSlotByIndex(2).getUnit()
                .orElseThrow().getId());
        assertEquals(13, ship().getCargo().getSlotByIndex(3).getUnit()
                .orElseThrow().getId());
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship().getCargo().getSlotByIndex(4).getGoods().orElseThrow());
    }

    private void reload() {
        data.setModel(model.save());
        Path file = directory.resolve("passenger.microcol");
        dao.saveToFile(file.toString(), data);
        data = dao.loadFromFile(file.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
    }

    private Unit addUnit(UnitType type, String owner, Location location) {
        return addUnit(100, type, owner, location);
    }

    private Unit addUnit(int id, UnitType type, String owner,
            Location location) {
        UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        if (type == UnitType.COLONIST || type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL
                || UnitType.EUROPEAN_SPECIALISTS.contains(type)
                || UnitType.SCHOOL_ONLY_SPECIALISTS.contains(type)) {
            unit.setWorkUpdates(0);
        }
        unit.setOwnerId(owner);
        unit.setAvailableMoves(3);
        unit.setAction(new UnitActionNoAction().save());
        unit.setPlaceMap(at(location));
        return model.createUnit(model.save(), unit);
    }

    @Test
    void selectedPassengerPaysOnePointWhileShipAndOtherCargoStay() {
        prepare(WATER, 3);
        reload();
        int shipMoves = ship().getActionPoints();
        passenger().disembarkToLocation(SHORE);
        assertEquals(SHORE, passenger().getLocation());
        assertTrue(slot().isEmpty());
        assertEquals(2, passenger().getActionPoints());
        otherCargoUnchanged();
        reload();
        passenger().embarkFromLocation(slot());
        assertSame(passenger(), slot().getUnit().orElseThrow());
        assertSame(slot(), passenger().getPlaceCargoSlot().getCargoSlot());
        assertEquals(1, passenger().getActionPoints());
        otherCargoUnchanged();
        reload();
        passenger().disembarkToLocation(SHORE);
        assertEquals(0, passenger().getActionPoints());
        unchangedOnRejection(() -> passenger().embarkFromLocation(slot()));
        assertEquals(shipMoves, ship().getActionPoints());
        otherCargoUnchanged();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 3 })
    void localPortAndPierTransfersPreservePointsAndIdentity(int moves) {
        prepare(PORT, moves);
        passenger().disembarkToLocation(PORT);
        assertTrue(passenger().isAtPlaceLocation());
        assertEquals(PORT, passenger().getLocation());
        assertEquals(moves, passenger().getActionPoints());
        slot().store(passenger());
        assertSame(passenger(), slot().getUnit().orElseThrow());
        assertEquals(moves, passenger().getActionPoints());
        reload();
        ship().placeToEuropePort(model.getEurope().getPort());
        passenger().placeToEuropePortPier();
        assertTrue(passenger().isAtEuropePier());
        assertTrue(slot().isEmpty());
        assertEquals(moves, passenger().getActionPoints());
        reload();
        slot().store(passenger());
        assertSame(slot(), passenger().getPlaceCargoSlot().getCargoSlot());
        assertSame(passenger(), slot().getUnit().orElseThrow());
        assertEquals(moves, passenger().getActionPoints());
        otherCargoUnchanged();
    }

    @Test
    void rejectsFullSlotWrongTurnAndInvalidPierWithoutMutation() {
        prepare(WATER, 1);
        unchangedOnRejection(() -> passenger().placeToEuropePortPier());
        unchangedOnRejection(
                () -> ship().getCargo().getSlotByIndex(1).store(passenger()));
        model.endTurn();
        unchangedOnRejection(() -> passenger().disembarkToLocation(SHORE));
        model.endTurn();
        data.setModel(model.save());
        data.getModel().getGameManager().setGameStarted(false);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        String passengerBefore = new Gson().toJson(passenger().save());
        String shipBefore = new Gson().toJson(ship().save());
        assertThrows(IllegalStateException.class,
                () -> passenger().disembarkToLocation(SHORE));
        assertEquals(passengerBefore, new Gson().toJson(passenger().save()));
        assertEquals(shipBefore, new Gson().toJson(ship().save()));
        assertFalse(model.isGameRunning());
        otherCargoUnchanged();
    }

    @Test
    void rejectsInvalidDistantWaterAndOccupiedShoreWithoutMutation() {
        prepare(WATER, 1);
        for (Location target : new Location[] { Location.of(0, 15),
                Location.of(51, 15), Location.of(25, 14), WATER }) {
            assertFalse(passenger().canUnitDisembarkAt(target));
            unchangedOnRejection(() -> passenger().disembarkToLocation(target));
        }
        addUnit(UnitType.COLONIST, "Dutch's King", SHORE);
        unchangedOnRejection(() -> passenger().disembarkToLocation(SHORE));
        otherCargoUnchanged();
    }

    @Test
    void rejectsForeignCarrierAndCargoSlotFromAnotherGame() {
        prepare(WATER, 2);
        passenger().disembarkToLocation(SHORE);
        Ship foreign = (Ship) addUnit(UnitType.GALLEON, "Dutch's King",
                Location.of(49, 16));
        assertFalse(
                passenger().canEmbark(foreign.getCargo().getSlotByIndex(0)));
        unchangedOnRejection(() -> passenger()
                .embarkFromLocation(foreign.getCargo().getSlotByIndex(0)));
        CargoSlot oldSlot = slot();
        reload();
        assertFalse(passenger().canEmbark(oldSlot));
        unchangedOnRejection(() -> passenger().embarkFromLocation(oldSlot));
    }

    @Test
    void zeroPointsBlockShoreButNotTheCarrierWithNoMoves() {
        prepare(WATER, 0);
        unchangedOnRejection(() -> passenger().disembarkToLocation(SHORE));
        prepare(WATER, 1);
        data.setModel(model.save());
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().setAvailableMoves(0);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(0, ship().getActionPoints());
        assertTrue(ship().getAvailableLocations().contains(SHORE));
        passenger().disembarkToLocation(SHORE);
        assertEquals(0, ship().getActionPoints());
        assertEquals(0, passenger().getActionPoints());
    }

    @Test
    void cancelledMovementLeavesPassengerPlacePointsAndCargo() {
        prepare(WATER, 2);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMoveStarted(UnitMoveStartedEvent event) {
                event.stopEventExecution();
            }
        });
        String before = snapshot();
        passenger().disembarkToLocation(SHORE);
        assertEquals(before, snapshot());
        passenger().placeToLocation(SHORE);
        before = snapshot();
        passenger().embarkFromLocation(slot());
        assertEquals(before, snapshot());
    }

    @Test
    void inlandColonyIsNotAPortEvenForAStoredShip() {
        prepare(PORT, 0);
        data.setModel(model.save());
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        PORT.getNeighbors().forEach(
                location -> terrain.put(location, TerrainType.GRASSLAND));
        data.getModel().getMap().setTerrainType(terrain);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertFalse(model.isOwnedCoastalPort(ship().getOwner(), PORT));
        unchangedOnRejection(() -> passenger().disembarkToLocation(PORT));
        assertFalse(ship().isPossibleToMoveAt(PORT));
    }

    @Test
    void everyAdjacentWaterDirectionMakesAnOwnedCoastalPort() {
        for (Location water : PORT.getNeighbors()) {
            prepare(PORT, 0);
            data.setModel(model.save());
            data.getModel().getColonies().forEach(colony -> {
                final ConstructionPo dock = new ConstructionPo();
                dock.setType(ConstructionType.DOCK);
                colony.getConstructions().add(dock);
            });
            Map<Location, TerrainType> terrain = data.getModel().getMap()
                    .getTerrainMap();
            PORT.getNeighbors().forEach(
                    location -> terrain.put(location, TerrainType.GRASSLAND));
            terrain.put(water, TerrainType.OCEAN);
            java.util.Set<Location> fields = data.getModel().getMap()
                    .getFieldSet();
            fields.remove(water);
            data.getModel().getMap().setFields(fields);
            java.util.Set<Location> trees = data.getModel().getMap()
                    .getTreeSet();
            trees.remove(water);
            data.getModel().getMap().setTrees(trees);
            data.getModel().getColonies().stream()
                    .filter(colony -> colony.getLocation().equals(PORT))
                    .flatMap(colony -> colony.getColonyFields().stream())
                    .filter(field -> field.getWorkerId() != null)
                    .forEach(field -> field
                            .setProducedGoodsType(GoodsType.CORN));
            data.getModel().getMap().setTerrainType(terrain);
            model = Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            assertTrue(model.isOwnedCoastalPort(ship().getOwner(), PORT),
                    water.toString());
            assertFalse(model.isOwnedCoastalPort(
                    model.getPlayerByName("Dutch's King"), PORT));
            assertFalse(model.isOwnedCoastalPort(ship().getOwner(), water));
            assertTrue(ship().isPossibleToMoveAt(PORT));
            passenger().disembarkToLocation(PORT);
            assertTrue(passenger().isAtPlaceLocation());
            assertEquals(0, passenger().getActionPoints());
            passenger().embarkFromLocation(slot());
            assertEquals(0, passenger().getActionPoints());
            assertSame(passenger(), slot().getUnit().orElseThrow());
            otherCargoUnchanged();
        }
    }

    @Test
    void falsePassengerIdentityCannotReplaceEitherCargoLink() {
        prepare(PORT, 1);
        Unit otherPassenger = model.getUnitById(15);
        passenger().disembarkToLocation(PORT);
        unchangedOnRejection(() -> passenger()
                .placeToCargoSlot(new PlaceCargoSlot(otherPassenger, slot())));
        assertTrue(slot().isEmpty());
        assertEquals(PORT, passenger().getLocation());
        otherCargoUnchanged();
    }

    @Test
    void removedPassengerAndRemovedCarrierCannotBeUsedByStaleReferences() {
        prepare(WATER, 2);
        Unit removed = passenger();
        removed.disembarkToLocation(SHORE);
        model.destroyUnit(removed);
        assertFalse(removed.canEmbark(slot()));
        unchangedOnRejection(() -> removed.embarkFromLocation(slot()));
        assertEquals(SHORE, removed.getLocation());
        assertEquals(1, removed.getActionPoints());
        assertTrue(slot().isEmpty());

        prepare(WATER, 2);
        passenger().disembarkToLocation(SHORE);
        CargoSlot removedSlot = slot();
        model.destroyUnit(ship());
        assertFalse(passenger().canEmbark(removedSlot));
        unchangedOnRejection(() -> passenger().embarkFromLocation(removedSlot));
        assertEquals(SHORE, passenger().getLocation());
        assertEquals(1, passenger().getActionPoints());
        assertTrue(removedSlot.isEmpty());
    }

    @Test
    void placeCargoSlotMustDescribeTheTargetSlot() {
        prepare(WATER, 3);
        passenger().disembarkToLocation(SHORE);
        CargoSlot other = ship().getCargo().getSlotByIndex(1);
        String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> slot().store(new PlaceCargoSlot(passenger(), other)));
        assertEquals(before, snapshot());
        assertTrue(slot().isEmpty());
        otherCargoUnchanged();
    }

    static Stream<UnitType> nestedCarrierTypes() {
        return Stream.of(UnitType.CARAVEL, UnitType.WAGON);
    }

    @ParameterizedTest
    @MethodSource("nestedCarrierTypes")
    void nestedCarrierCannotBoardShipThroughAnyPathWithoutMutation(
            UnitType carrierType) {
        prepare(WATER, 3);
        passenger().disembarkToLocation(SHORE);
        Unit nested = addUnit(carrierType, ship().getOwner().getName(), SHORE);
        CargoSlot target = slot();
        String before = snapshot();
        int nestedMoves = nested.getActionPoints();
        int shipMoves = ship().getActionPoints();
        assertFalse(nested.canEmbark(target));
        assertThrows(IllegalStateException.class, () -> target.store(nested));
        IllegalStateException rejected = assertThrows(
                IllegalStateException.class,
                () -> target.store(new PlaceCargoSlot(nested, target)));
        assertTrue(rejected.getMessage().contains("cannot be passenger"));
        assertThrows(IllegalStateException.class,
                () -> nested.embarkFromLocation(target));
        assertEquals(before, snapshot());
        assertEquals(SHORE, nested.getLocation());
        assertEquals(nestedMoves, nested.getActionPoints());
        assertEquals(shipMoves, ship().getActionPoints());
        assertTrue(target.isEmpty());
        otherCargoUnchanged();
    }

    @ParameterizedTest
    @MethodSource("nestedCarrierTypes")
    void nestedCarrierCannotBoardWagonThroughAnyPathWithoutMutation(
            UnitType carrierType) {
        prepare(WATER, 3);
        passenger().disembarkToLocation(SHORE);
        addUnit(101, UnitType.WAGON, ship().getOwner().getName(), SHORE);
        Unit nested = addUnit(carrierType, ship().getOwner().getName(), SHORE);
        CargoSlot target = ((UnitWithCargo) model.getUnitById(101)).getCargo()
                .getSlotByIndex(0);
        String before = snapshot();
        int nestedMoves = nested.getActionPoints();
        assertFalse(nested.canEmbark(target));
        assertThrows(IllegalStateException.class, () -> target.store(nested));
        IllegalStateException rejected = assertThrows(
                IllegalStateException.class,
                () -> target.store(new PlaceCargoSlot(nested, target)));
        assertTrue(rejected.getMessage().contains("cannot be passenger"));
        assertThrows(IllegalStateException.class,
                () -> nested.embarkFromLocation(target));
        assertEquals(before, snapshot());
        assertEquals(SHORE, nested.getLocation());
        assertEquals(nestedMoves, nested.getActionPoints());
        assertTrue(target.isEmpty());
        otherCargoUnchanged();
    }

    @Test
    void savedPassengerInShipHoldRestoresIdentityAndPoints() {
        prepare(WATER, 3);
        passenger().disembarkToLocation(SHORE);
        passenger().embarkFromLocation(slot());
        reload();
        assertSame(passenger(), slot().getUnit().orElseThrow());
        assertSame(slot(), passenger().getPlaceCargoSlot().getCargoSlot());
        assertEquals(1, passenger().getActionPoints());
        otherCargoUnchanged();
    }

    @Test
    void savedColonistInWagonHoldIsRejectedOnRestore() {
        prepare(WATER, 3);
        passenger().disembarkToLocation(SHORE);
        addUnit(101, UnitType.WAGON, ship().getOwner().getName(), SHORE);
        data.setModel(model.save());
        UnitPo passengerPo = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 16).findFirst().orElseThrow();
        passengerPo.setPlaceMap(null);
        passengerPo.setPlaceCargoSlot(new PlaceCargoSlotPo());
        data.getModel().getUnits().stream().filter(u -> u.getId() == 101)
                .findFirst().orElseThrow().getCargo().getSlots().get(0)
                .setUnitId(16);
        assertThrows(IllegalStateException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @ParameterizedTest
    @MethodSource("nestedCarrierTypes")
    void savedNestedCarrierInShipHoldIsRejectedOnRestore(UnitType carrierType) {
        prepare(WATER, 3);
        addUnit(carrierType, ship().getOwner().getName(), SHORE);
        data.setModel(model.save());
        UnitPo passengerPo = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 16).findFirst().orElseThrow();
        passengerPo.setPlaceCargoSlot(null);
        passengerPo.setPlaceMap(at(SHORE));
        UnitPo nestedPo = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 100).findFirst().orElseThrow();
        nestedPo.setPlaceMap(null);
        nestedPo.setPlaceCargoSlot(new PlaceCargoSlotPo());
        data.getModel().getUnits().stream().filter(u -> u.getId() == 3)
                .findFirst().orElseThrow().getCargo().getSlots().get(0)
                .setUnitId(100);
        assertThrows(IllegalStateException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

}
