package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

import com.google.gson.Gson;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.turnevent.TurnEventFaminePlagueColony;

class ColonyFamineTest {

    private Model model(final TerrainType foodTerrain) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(Location.of(24, 14), foodTerrain);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(java.util.Collections.emptySet());
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void survivorFoodIsCommittedAfterOneRandomFamineDeath() throws Exception {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainTwoResidents(model, colony);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertEquals(-1, colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter());
        assertTrue(colony.getColonyFieldInDirection(Direction.southWest)
                .getProduction().orElseThrow().getAmount() > 0);
        chooseVictim(colony, model.getUnitById(1));

        colony.startTurn();

        assertEquals(1, colony.getUnitsInsideColony().size());
        assertEquals(0, colony.getUnitsInsideColony().get(0).getId());
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        final List<TurnEventFaminePlagueColony> losses = model
                .getTurnEventStore().getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventFaminePlagueColony.class::isInstance)
                .map(TurnEventFaminePlagueColony.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, losses.size());
        assertEquals(1, losses.get(0).getVictimId());
        assertEquals(1,
                losses.get(0).getStocks().stream()
                        .filter(goods -> goods.getType().equals(GoodsType.CORN))
                        .findFirst().orElseThrow().getAmount());
        assertEquals(1, model.save().getTurnEvents().stream()
                .filter(event -> event.getFamineVictim() != null).count());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 20));
        assertEquals(1,
                losses.get(0).getStocks().stream()
                        .filter(goods -> goods.getType().equals(GoodsType.CORN))
                        .findFirst().orElseThrow().getAmount());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(1, restored.getTurnEventStore()
                .getForPlayer(
                        restored.getColonyById(0).orElseThrow().getOwner())
                .stream().filter(TurnEventFaminePlagueColony.class::isInstance)
                .count());
    }

    @Test
    void foodWorkerVictimLeavesNoPhantomProduction() throws Exception {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainTwoResidents(model, colony);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        chooseVictim(colony, model.getUnitById(0));
        colony.startTurn();
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertEquals(1, colony.getUnitsInsideColony().get(0).getId());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
    }

    @Test
    void indoorVictimRemovesItsOutput() throws Exception {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainResidents(model, colony, 0, 5);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        chooseVictim(colony, model.getUnitById(5));
        final int cigarsBefore = colony.getWarehouse()
                .getGoods(GoodsType.CIGARS).getAmount();
        final int tobaccoBefore = colony.getWarehouse()
                .getGoods(GoodsType.TOBACCO).getAmount();
        colony.startTurn();
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertEquals(0, colony.getUnitsInsideColony().get(0).getId());
        assertEquals(cigarsBefore,
                colony.getWarehouse().getGoods(GoodsType.CIGARS).getAmount());
        assertEquals(tobaccoBefore,
                colony.getWarehouse().getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
    }

    @Test
    void outdoorInputVictimLeavesOnlyAvailableInputForIndoorWorker()
            throws Exception {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainResidents(model, colony, 0, 1, 5);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 2));
        final int cigarsBefore = colony.getWarehouse()
                .getGoods(GoodsType.CIGARS).getAmount();
        chooseVictim(colony, model.getUnitById(1));

        colony.startTurn();

        assertEquals(2, colony.getUnitsInsideColony().size());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(cigarsBefore + 2,
                colony.getWarehouse().getGoods(GoodsType.CIGARS).getAmount());
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            assertTrue(colony.getWarehouse().getGoods(type).getAmount() >= 0);
        }
    }

    @Test
    void outsideVisitorAndCargoPassengersSurviveFamine() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final UnitPo shipData = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        shipData.setPlaceHighSeas(null);
        shipData.setDepartureLocation(null);
        shipData.setDepartureDirection(null);
        final PlaceMapPo port = new PlaceMapPo();
        port.setLocation(Location.of(25, 14));
        port.setOrientation(Direction.west);
        shipData.setPlaceMap(port);
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(Location.of(25, 13), TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit visitor = model.addUnitOutSideColony(colony);
        final Ship ship = (Ship) model.getUnitById(3);
        final List<Integer> passengerIds = ship.getCargo().getSlots().stream()
                .filter(slot -> slot.getUnit().isPresent())
                .map(slot -> slot.getUnit().get().getId())
                .collect(Collectors.toList());
        assertFalse(passengerIds.isEmpty());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        colony.startTurn();
        assertTrue(colony.getUnitsOutSideColony().contains(visitor));
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(Location.of(25, 14), ship.getLocation());
        assertEquals(passengerIds,
                ship.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnit().isPresent())
                        .map(slot -> slot.getUnit().get().getId())
                        .collect(Collectors.toList()));
        for (final int id : passengerIds) {
            assertSame(ship, model.getUnitById(id).getPlaceCargoSlot()
                    .getCargoSlot().getOwnerUnit());
        }
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            assertTrue(colony.getWarehouse().getGoods(type).getAmount() >= 0);
        }
    }

    @Test
    void completedNationalBoundaryDoesNotReplayFamine() {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        model.endTurn();
        model.endTurn();
        final long losses = model.getTurnEventStore()
                .getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventFaminePlagueColony.class::isInstance).count();
        assertEquals(1, losses);
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String saved = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(saved, new Gson().toJson(resumed.save()));
        assertEquals(1, resumed.getTurnEventStore()
                .getForPlayer(resumed.getColonyById(0).orElseThrow().getOwner())
                .stream().filter(TurnEventFaminePlagueColony.class::isInstance)
                .count());
    }

    @Test
    void exactlyZeroFoodDoesNotKill() {
        final Model model = model(TerrainType.GRASSLAND);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainTwoResidents(model, colony);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertEquals(0, colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter());
        colony.startTurn();
        assertEquals(2, colony.getUnitsInsideColony().size());
        assertEquals(0, model.getTurnEventStore()
                .getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventFaminePlagueColony.class::isInstance).count());
    }

    @Test
    void finalResidentLossRecordsDestroyedColonyWithoutStocks() {
        final Model model = model(TerrainType.TUNDRA);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retainTwoResidents(model, colony);
        final Unit foodWorker = model.getUnitById(0);
        foodWorker.getPlace().destroy();
        model.destroyUnit(foodWorker);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));

        colony.startTurn();

        assertFalse(colony.isValid());
        final TurnEventFaminePlagueColony loss = model.getTurnEventStore()
                .getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventFaminePlagueColony.class::isInstance)
                .map(TurnEventFaminePlagueColony.class::cast).findFirst()
                .orElseThrow();
        assertEquals(1, loss.getVictimId());
        assertTrue(loss.getStocks().isEmpty());
    }

    private void chooseVictim(final Colony colony, final Unit victim)
            throws Exception {
        final int victimIndex = colony.getUnitsInsideColony().indexOf(victim);
        final Field field = Colony.class.getDeclaredField("random");
        field.setAccessible(true);
        final Random random = (Random) field.get(colony);
        long seed = 0;
        while (new Random(seed)
                .nextInt(colony.getUnitsInsideColony().size()) != victimIndex) {
            seed++;
        }
        random.setSeed(seed);
    }

    private void retainTwoResidents(final Model model, final Colony colony) {
        retainResidents(model, colony, 0, 1);
    }

    private void retainResidents(final Model model, final Colony colony,
            final int... selected) {
        final List<Unit> residents = colony.getUnitsInsideColony();
        for (final Unit unit : residents) {
            if (java.util.Arrays.stream(selected)
                    .noneMatch(id -> id == unit.getId())) {
                unit.getPlace().destroy();
                model.destroyUnit(unit);
            }
        }
    }
}
