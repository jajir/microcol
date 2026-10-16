package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.turnevent.TurnEventColonyWasDestroyed;
import org.microcol.model.unit.Ship;

class ColonyDestructionTest {

    private ModelPo fixture() {
        return new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel();
    }

    private Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Ship newShip(final Model model, final Colony colony) {
        model.addUnitInEurope(UnitType.GALLEON, colony.getOwner());
        final Ship ship = (Ship) model.getAllUnits()
                .get(model.getAllUnits().size() - 1);
        ship.placeToLocation(colony.getLocation());
        return ship;
    }

    @Test
    void dockedShipsAndCargoMoveTogetherAndRemovalIsIdempotent() {
        final Model model = load(fixture());
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final Player owner = colony.getOwner();
        final Ship alreadyThere = (Ship) model.getUnitById(3);
        alreadyThere.placeToLocation(Location.of(21, 11));
        final Ship first = newShip(model, colony);
        final Ship second = newShip(model, colony);
        final Unit passenger = model.addUnitOutSideColony(colony);
        first.getCargo().getEmptyCargoSlot().orElseThrow().store(passenger);
        assertEquals(1, first.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).count());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.SILK, 5));
        first.getCargo().getSlots().get(1)
                .storeFromColonyWarehouse(Goods.of(GoodsType.SILK, 5), colony);
        assertEquals(Goods.of(GoodsType.SILK, 5),
                first.getCargo().getSlots().get(1).getGoods().orElseThrow());
        final Unit outside = model.addUnitOutSideColony(colony);
        final int moves = first.getActionPoints();

        colony.getColonyBuildingQueue().addAtEnd(UnitType.WAGON);
        colony.getUnitsInsideColony()
                .forEach(worker -> worker.placeToLocation(Location.of(19, 11)));
        model.destroyColony(colony);
        model.destroyColony(colony);

        assertFalse(colony.isValid());
        assertEquals(Location.of(21, 11), first.getLocation());
        assertEquals(Location.of(21, 11), second.getLocation());
        assertEquals(Location.of(21, 11), alreadyThere.getLocation());
        assertEquals(moves, first.getActionPoints());
        assertSame(passenger,
                first.getCargo().getSlots().get(0).getUnit().orElseThrow());
        assertEquals(Goods.of(GoodsType.SILK, 5),
                first.getCargo().getSlots().get(1).getGoods().orElseThrow());
        assertTrue(model.getAllUnits().contains(outside));
        assertEquals(1, model.getTurnEventStore().getForPlayer(owner).stream()
                .filter(TurnEventColonyWasDestroyed.class::isInstance).count());
        final Model restored = load(model.save());
        assertTrue(restored.getColonyByName("Delft").isEmpty());
        assertEquals(3, restored.getUnitsAt(Location.of(21, 11)).stream()
                .filter(Unit::isShip).count());
        assertEquals(1, ((Ship) restored.getUnitById(first.getId())).getCargo()
                .getSlots().stream().filter(CargoSlot::isLoadedUnit).count());
        assertEquals(Goods.of(GoodsType.SILK, 5),
                ((Ship) restored.getUnitById(first.getId())).getCargo()
                        .getSlots().get(1).getGoods().orElseThrow());
        restored.startGame();
        restored.endTurn();
        assertTrue(restored.getColonyByName("Delft").isEmpty());
    }

    @Test
    void blockedPortLosesShipAndPassengerWithPersistedReport() {
        final Model prepared = load(fixture());
        final Colony preparedColony = prepared.getColonyByName("Delft")
                .orElseThrow();
        final Ship preparedShip = newShip(prepared, preparedColony);
        final Unit preparedPassenger = prepared
                .addUnitOutSideColony(preparedColony);
        preparedShip.getCargo().getEmptyCargoSlot().orElseThrow()
                .store(preparedPassenger);
        preparedColony.getWarehouse().setGoods(Goods.of(GoodsType.SILK, 5));
        preparedShip.getCargo().getSlots().get(1).storeFromColonyWarehouse(
                Goods.of(GoodsType.SILK, 5), preparedColony);
        final ModelPo data = prepared.save();
        final Location port = Location.of(22, 12);
        final Map<Location, TerrainType> terrain = data.getMap()
                .getTerrainMap();
        port.getNeighbors().forEach(
                neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
        data.getMap().setTerrainType(terrain);
        final Model model = load(data);
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final Ship ship = (Ship) model.getUnitById(preparedShip.getId());
        final Unit passenger = model.getUnitById(preparedPassenger.getId());
        assertEquals(Goods.of(GoodsType.SILK, 5),
                ship.getCargo().getSlots().get(1).getGoods().orElseThrow());
        colony.getUnitsInsideColony()
                .forEach(worker -> worker.placeToLocation(Location.of(19, 11)));
        model.destroyColony(colony);
        assertFalse(model.getAllUnits().contains(ship));
        assertFalse(model.getAllUnits().contains(passenger));
        final List<TurnEventColonyWasDestroyed> reports = model
                .getTurnEventStore().getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventColonyWasDestroyed.class::isInstance)
                .map(TurnEventColonyWasDestroyed.class::cast)
                .collect(java.util.stream.Collectors.toList());
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).getLostShips());
        final Model restored = load(model.save());
        assertTrue(restored.getColonyByName("Delft").isEmpty());
        assertFalse(restored.getAllUnits().stream()
                .anyMatch(unit -> unit.getId() == ship.getId()
                        || unit.getId() == passenger.getId()));
        assertEquals(1, restored.getTurnEventStore()
                .getForPlayer(
                        restored.getPlayerByName(colony.getOwner().getName()))
                .stream().filter(TurnEventColonyWasDestroyed.class::isInstance)
                .map(TurnEventColonyWasDestroyed.class::cast).findFirst()
                .orElseThrow().getLostShips());
    }

    @Test
    void enemyShipBlocksFirstWaterSquare() {
        final Model model = load(fixture());
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final Ship docked = newShip(model, colony);
        final Player king = model.getPlayerByName("Dutch's King");
        final Ship enemy = (Ship) model.createCargoShipForKing(king);
        enemy.placeToLocation(Location.of(21, 11));

        colony.getUnitsInsideColony()
                .forEach(worker -> worker.placeToLocation(Location.of(19, 11)));

        assertEquals(Location.of(22, 11), docked.getLocation());
        assertEquals(Location.of(21, 11), enemy.getLocation());
        assertEquals(0,
                model.getTurnEventStore().getForPlayer(colony.getOwner())
                        .stream()
                        .filter(TurnEventColonyWasDestroyed.class::isInstance)
                        .map(TurnEventColonyWasDestroyed.class::cast)
                        .findFirst().orElseThrow().getLostShips());
    }

    @Test
    void validMapEdgeWaterIsAnEscapeDestination() {
        final ModelPo data = fixture();
        data.getColonies().stream()
                .filter(saved -> "Delft".equals(saved.getName())).findFirst()
                .orElseThrow().setLocation(Location.of(2, 2));
        final Map<Location, TerrainType> terrain = data.getMap()
                .getTerrainMap();
        Location.of(2, 2).getNeighbors().forEach(
                neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
        terrain.put(Location.of(1, 2), TerrainType.OCEAN);
        data.getMap().setTerrainType(terrain);
        final Model model = load(data);
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final Ship ship = newShip(model, colony);

        colony.getUnitsInsideColony()
                .forEach(worker -> worker.placeToLocation(Location.of(19, 11)));
        model.destroyColony(colony);

        assertEquals(Location.of(1, 2), ship.getLocation());
        assertFalse(colony.isValid());
        assertTrue(load(model.save()).getColonyByName("Delft").isEmpty());
    }
}
