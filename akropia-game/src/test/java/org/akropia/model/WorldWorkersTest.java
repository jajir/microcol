package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ColonyPo;
import org.akropia.model.store.ConstructionPo;
import com.google.gson.Gson;

class WorldWorkersTest {
    private Model fixture(final boolean fishing) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        final ColonyPo colony = data.getModel().getColonies().get(0);
        colony.getLocation().getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        if (fishing) {
            terrain.put(colony.getLocation().add(Direction.west.getVector()),
                    TerrainType.OCEAN);
            final ConstructionPo dock = new ConstructionPo();
            dock.setType(ConstructionType.DOCK);
            colony.getConstructions().add(dock);
        }
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Collections.emptySet());
        data.getModel().getMap().setFields(Collections.emptySet());
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void aWorkerHasOneMapPositionAndRetainsItsJobAfterLoad() {
        final Model model = fixture(false);
        final Unit worker = model.getUnitById(0);
        final Location at = worker.getPlaceColonyField().getColony()
                .getColonyFieldInDirection(Direction.west).getLocation();
        assertEquals(at, worker.getLocation());
        assertEquals(1, model.getUnitsAt(at).stream()
                .filter(unit -> unit == worker).count());
        ((AbstractUnit) worker).setActionPoints(0);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(at, restored.getUnitById(0).getLocation());
        assertEquals("w", restored.getUnitById(0).getTaskSign());
        assertEquals(0, restored.getUnitById(0).getMovementThirds());
    }

    @Test
    void cancellingAndReassigningAtZeroDoesNotMoveOrProduceGoods() {
        final Model model = fixture(false);
        final Unit worker = model.getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        final ColonyField field = colony
                .getColonyFieldInDirection(Direction.west);
        final Goods warehouse = colony.getWarehouse().getGoods(GoodsType.GRAIN);
        ((AbstractUnit) worker).setActionPoints(0);
        worker.cancelColonyWork();
        assertEquals(field.getLocation(), worker.getLocation());
        assertEquals("", worker.getTaskSign());
        assertTrue(field.isEmpty());
        worker.placeToColonyField(field, GoodsType.GRAIN);
        assertEquals(0, worker.getMovementThirds());
        assertEquals("w", worker.getTaskSign());
        assertEquals(warehouse,
                colony.getWarehouse().getGoods(GoodsType.GRAIN));
    }

    @Test
    void zeroMovementRefusesReturnWithoutPartialEffects() {
        final Model model = fixture(false);
        final Unit worker = model.getUnitById(0);
        ((AbstractUnit) worker).setActionPoints(0);
        final String before = new Gson().toJson(model.save());
        assertFalse(worker.canReturnToWorkingColony());
        assertThrows(IllegalStateException.class,
                worker::returnToWorkingColony);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void ordinaryMovementEndsTheJobAndPaysTerrainCost() {
        final Model model = fixture(false);
        final Unit worker = model.getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        final Location target = colony
                .getColonyFieldInDirection(Direction.northWest).getLocation();
        final int cost = worker.getEntryCostThirds(worker.getLocation(),
                target);
        ((AbstractUnit) worker).setActionPoints(1);
        worker.moveOneStep(target);
        assertEquals(target, worker.getLocation());
        assertEquals(3 - cost, worker.getMovementThirds());
        assertTrue(worker.getWorkingColony().isEmpty());
        assertEquals("", worker.getTaskSign());
        assertTrue(colony.getColonyFieldInDirection(Direction.west).isEmpty());
    }

    @Test
    void mapOffersChooseTheRecipientAndDoNotMutateTheWorld() {
        final Model model = fixture(false);
        final Unit worker = model.getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        worker.cancelColonyWork();
        ((AbstractUnit) worker).setActionPoints(0);
        final String before = new Gson().toJson(model.save());
        final ColonyJobChoice choice = worker.getMapJobChoices().stream()
                .filter(job -> job.getColony() == colony
                        && job.getGoodsType() == GoodsType.GRAIN)
                .findFirst().orElseThrow();
        assertEquals(before, new Gson().toJson(model.save()));
        choice.execute();
        assertSame(colony, worker.getWorkingColony().orElseThrow());
        assertEquals(0, worker.getMovementThirds());
    }

    @Test
    void fishermanCannotUseOrdinaryMapMovesAndReturnsForOnePoint() {
        final Model model = fixture(true);
        final Unit worker = model.getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        ((AbstractUnit) worker).setActionPoints(1);
        assertTrue(worker.isFishing());
        assertTrue(worker.getMapJobChoices().isEmpty());
        assertThrows(IllegalStateException.class,
                () -> worker.moveOneStep(colony.getLocation()));
        worker.returnToWorkingColony();
        assertEquals(colony.getLocation(), worker.getLocation());
        assertEquals(0, worker.getActionPoints());
        assertFalse(worker.isFishing());
    }

    @Test
    void cancelledFishingStaysAtSeaAndSurvivesSaveLoad() {
        final Model model = fixture(true);
        final Unit worker = model.getUnitById(0);
        final Location at = worker.getLocation();
        ((AbstractUnit) worker).setActionPoints(0);
        worker.cancelColonyWork();
        assertEquals(at, worker.getLocation());
        assertTrue(worker.isFishing());
        assertEquals("", worker.getTaskSign());
        final Unit loaded = Model
                .make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                .getUnitById(0);
        assertTrue(loaded.isFishing());
        assertEquals(at, loaded.getLocation());
        assertEquals(0, loaded.getMovementThirds());
    }

    @Test
    void lossOfHomeRescuesFishermanAtZeroAndLeavesLandWorkersInPlace() {
        final Model model = fixture(true);
        final Unit fisherman = model.getUnitById(0);
        final Unit landWorker = model.getUnitById(1);
        final Colony colony = fisherman.getWorkingColony().orElseThrow();
        final Player owner = fisherman.getOwner();
        final Location land = landWorker.getLocation();
        ((AbstractUnit) fisherman).setActionPoints(0);
        model.releaseOutdoorWorkers(colony, true);
        assertSame(owner, fisherman.getOwner());
        assertFalse(fisherman.isFishing());
        assertTrue(model.getMap().getTerrainTypeAt(fisherman.getLocation())
                .isLand());
        assertTrue(model.getColonyAt(fisherman.getLocation()).isEmpty());
        assertEquals(0, fisherman.getMovementThirds());
        assertEquals(land, landWorker.getLocation());
        assertTrue(landWorker.getWorkingColony().isEmpty());
    }

    private Model fishingBattle(final boolean fullCargo) {
        final org.akropia.model.store.ModelPo saved = fixture(true).save();
        final var freePlay = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel();
        saved.getGameManager().setPlayers(
                new java.util.ArrayList<>(saved.getGameManager().getPlayers()));
        freePlay.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("France")
                        || player.getName().equals("France's King"))
                .forEach(saved.getGameManager().getPlayers()::add);
        saved.setEuropeanRelations(freePlay.getEuropeanRelations());
        saved.getGameManager().setCurrentPlayer("France");
        final Location at = Location.of(24, 13);
        final Map<Location, TerrainType> terrain = saved.getMap()
                .getTerrainMap();
        terrain.put(at, TerrainType.OCEAN);
        saved.getMap().setTerrainType(terrain);
        final var ship = new org.akropia.model.store.UnitPo();
        ship.setId(900);
        ship.setType(UnitType.FRIGATE);
        ship.setOwnerId("France");
        ship.setAvailableMoves(UnitType.FRIGATE.getSpeed());
        ship.setAction(new org.akropia.model.unit.UnitActionNoAction().save());
        final var place = new org.akropia.model.store.PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.south);
        ship.setPlaceMap(place);
        final var cargo = new org.akropia.model.store.CargoPo();
        for (int slot = 0; slot < UnitType.FRIGATE.getCargoCapacity(); slot++) {
            final var hold = new org.akropia.model.store.CargoSlotPo();
            if (fullCargo) {
                hold.setGoodsType(GoodsType.GRAIN);
                hold.setAmount(100);
            }
            cargo.getSlots().add(hold);
        }
        ship.setCargo(cargo);
        saved.getUnits().add(ship);
        final Model model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.getPlayerByName("France")
                .revealMapForUnit(model.getUnitById(900));
        return model;
    }

    @Test
    void victoriousShipCapturesTheFishermanIntoItsCargo() {
        final Model model = fishingBattle(false);
        final Unit fisherman = model.getUnitById(0);
        final Unit attacker = model.getUnitById(900);
        final AttackOrder order = attacker
                .prepareAttack(fisherman.getLocation(), true).orElseThrow();
        ColonyBattles.setState(model,
                CombatDraws.stateFor(order.getCombatStrengths().orElseThrow()
                        .getAttackerWinProbability(), true));
        order.execute();
        assertSame(attacker.getOwner(), fisherman.getOwner());
        assertTrue(fisherman.isAtCargoSlot());
        assertTrue(fisherman.getWorkingColony().isEmpty());
        assertEquals(0, fisherman.getMovementThirds());
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(loaded.getUnitById(0).isAtCargoSlot());
    }

    @Test
    void fullCargoDrownsTheFishermanAndReportsItExactlyOnce() {
        final Model model = fishingBattle(true);
        final Unit fisherman = model.getUnitById(0);
        final Player owner = fisherman.getOwner();
        final AttackOrder order = model.getUnitById(900)
                .prepareAttack(fisherman.getLocation(), true).orElseThrow();
        ColonyBattles.setState(model,
                CombatDraws.stateFor(order.getCombatStrengths().orElseThrow()
                        .getAttackerWinProbability(), true));
        order.execute();
        assertTrue(model.tryGetUnitById(0).isEmpty());
        assertEquals(1,
                model.getTurnEventsLocalizedMessages(owner).stream().filter(
                        org.akropia.model.turnevent.TurnEventFishermanLost.class::isInstance)
                        .count());
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(1, loaded
                .getTurnEventsLocalizedMessages(
                        loaded.getPlayerByName(owner.getName()))
                .stream()
                .filter(org.akropia.model.turnevent.TurnEventFishermanLost.class::isInstance)
                .count());
    }

    @Test
    void canceledFishermanCanBeAttackedAndCannotStartAnOrder() {
        final Model model = fishingBattle(false);
        final Unit fisherman = model.getUnitById(0);
        ColonyBattles.setCurrentPlayer(model, fisherman.getOwner());
        fisherman.cancelColonyWork();
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> ((AbstractUnit) fisherman).setWaitingOrder(
                        org.akropia.model.unit.UnitActionType.sentry));
        assertEquals(before, new Gson().toJson(model.save()));
        ColonyBattles.setCurrentPlayer(model, model.getPlayerByName("France"));
        assertTrue(model.getUnitById(900)
                .prepareAttack(fisherman.getLocation(), true).isPresent());
    }

    @Test
    void aFishingBoatCannotMoveDirectlyToAnotherLandJob() {
        final Model model = fixture(true);
        final Unit fisherman = model.getUnitById(0);
        fisherman.startTurn();
        final Colony colony = fisherman.getFishingColony().orElseThrow();
        final String before = new Gson().toJson(model.save());
        final ColonyField land = colony
                .getColonyFieldInDirection(Direction.north);
        assertFalse(land.canAccept(fisherman, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> fisherman.placeToColonyField(land, GoodsType.GRAIN));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void rescueWithoutAnyFreeLandDrownsOnceAndSurvivesLoad() {
        final Model original = fixture(true);
        final var saved = original.save();
        final java.util.Set<Location> occupiedLand = original.getAllUnits()
                .stream().filter(Unit::isAtPlaceLocation).map(Unit::getLocation)
                .filter(at -> original.getMap().getTerrainTypeAt(at).isLand())
                .collect(java.util.stream.Collectors.toSet());
        original.getColonies()
                .forEach(colony -> occupiedLand.add(colony.getLocation()));
        final Map<Location, TerrainType> terrain = saved.getMap()
                .getTerrainMap();
        terrain.replaceAll((at, old) -> occupiedLand.contains(at)
                ? TerrainType.GRASSLAND
                : old == TerrainType.HIGH_SEA ? old : TerrainType.OCEAN);
        saved.getMap().setTerrainType(terrain);
        saved.getMap().setTrees(Collections.emptySet());
        saved.getMap().setFields(Collections.emptySet());
        saved.getMap().setRoads(Collections.emptySet());
        final Model model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = model.getUnitById(0).getFishingColony()
                .orElseThrow();
        final Player owner = colony.getOwner();
        model.releaseOutdoorWorkers(colony, true);
        model.releaseOutdoorWorkers(colony, true);
        assertTrue(model.tryGetUnitById(0).isEmpty());
        assertEquals(1,
                model.getTurnEventsLocalizedMessages(owner).stream().filter(
                        org.akropia.model.turnevent.TurnEventFishermanLost.class::isInstance)
                        .count());
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(loaded.tryGetUnitById(0).isEmpty());
    }

    @Test
    void multipleFishermenUseDistinctDeterministicRescueSquares() {
        final var saved = fixture(true).save();
        final var colonyPo = saved.getColonies().get(0);
        final var secondField = colonyPo.getColonyFields().stream()
                .filter(field -> Integer.valueOf(1).equals(field.getWorkerId()))
                .findFirst().orElseThrow();
        secondField.setProducedGoodsType(GoodsType.GRAIN);
        final Map<Location, TerrainType> terrain = saved.getMap()
                .getTerrainMap();
        terrain.put(colonyPo.getLocation().add(secondField.getDirection()),
                TerrainType.OCEAN);
        saved.getMap().setTerrainType(terrain);
        final Model first = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Model second = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        first.releaseOutdoorWorkers(first.getColonyById(0).orElseThrow(), true);
        second.releaseOutdoorWorkers(second.getColonyById(0).orElseThrow(),
                true);
        assertNotEquals(first.getUnitById(0).getLocation(),
                first.getUnitById(1).getLocation());
        for (int id : new int[] { 0, 1 }) {
            assertEquals(first.getUnitById(id).getLocation(),
                    second.getUnitById(id).getLocation());
            assertEquals(0, first.getUnitById(id).getMovementThirds());
            assertTrue(first.getColonyAt(first.getUnitById(id).getLocation())
                    .isEmpty());
            assertEquals(1, first
                    .getUnitsAt(first.getUnitById(id).getLocation()).size());
        }
    }

    private Model guardedFishing(final UnitType shipType) {
        final Model original = fishingBattle(false);
        final var saved = original.save();
        final var guard = new org.akropia.model.store.UnitPo();
        guard.setAction(new org.akropia.model.unit.UnitActionNoAction().save());
        final var place = new org.akropia.model.store.PlaceMapPo();
        place.setLocation(original.getUnitById(0).getLocation());
        place.setOrientation(Direction.east);
        guard.setPlaceMap(place);
        guard.setId(901);
        guard.setType(shipType);
        guard.setAvailableMoves(shipType.getSpeed());
        guard.setOwnerId("Dutch");
        guard.getPlaceMap().setLocation(original.getUnitById(0).getLocation());
        final var cargo = new org.akropia.model.store.CargoPo();
        for (int i = 0; i < shipType.getCargoCapacity(); i++) {
            cargo.getSlots().add(new org.akropia.model.store.CargoSlotPo());
        }
        guard.setCargo(cargo);
        saved.getUnits().add(guard);
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void ownWarshipDefendsFishermanBeforeTheWorker() {
        final Model model = guardedFishing(UnitType.FRIGATE);
        final AttackOrder order = model.getUnitById(900)
                .prepareAttack(model.getUnitById(0).getLocation(), true)
                .orElseThrow();
        assertEquals(901, order.getDefender().getId());
        assertTrue(order.getColonyDefense().isEmpty());
    }

    @Test
    void aMerchantShipDoesNotStandInForACombatReadyEscort() {
        final Model model = guardedFishing(UnitType.GALLEON);
        final AttackOrder order = model.getUnitById(900)
                .prepareAttack(model.getUnitById(0).getLocation(), true)
                .orElseThrow();
        assertEquals(0, order.getDefender().getId());
        assertEquals(ColonyDefense.Kind.FIELD,
                order.getColonyDefense().orElseThrow().getKind());
    }

    @Test
    void mountedWorkerPaysTheQuotedJourneyBeforeReturningItsHorse() {
        final Model model = fixture(false);
        final var worker = (org.akropia.model.unit.UnitFreeColonist) model
                .getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        worker.placeToLocation(colony.getLocation());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HORSE, 0));
        worker.setMounted(true);
        worker.startTurn();
        final int horses = colony.getWarehouse().getGoods(GoodsType.HORSE)
                .getAmount();
        final ColonyField target = colony
                .getColonyFieldInDirection(Direction.east);
        assertTrue(target.canAccept(worker, GoodsType.GRAIN));
        worker.placeToColonyField(target, GoodsType.GRAIN);
        assertFalse(worker.isMounted());
        assertEquals(horses
                + org.akropia.model.unit.UnitFreeColonist.REQUIRED_HORSES_FOR_MOUNTED_UNIT,
                colony.getWarehouse().getGoods(GoodsType.HORSE).getAmount());
        assertEquals(3, worker.getMovementThirds());
        assertEquals(target.getLocation(), worker.getLocation());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(Model
                        .make(model.save(),
                                MissionImpl.getPredefinedGameOverEvaluators())
                        .save()));
    }

    @Test
    void unaffordableJobDoesNotReturnEquipmentOrChangeTheWorker() {
        final Model model = fixture(false);
        final var worker = (org.akropia.model.unit.UnitFreeColonist) model
                .getUnitById(0);
        final Colony colony = worker.getWorkingColony().orElseThrow();
        worker.placeToLocation(colony.getLocation());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HORSE, 0));
        worker.setMounted(true);
        ((AbstractUnit) worker).setActionPoints(0);
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(
                        colony.getColonyFieldInDirection(Direction.east),
                        GoodsType.GRAIN));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void fishingCancellationHonorsTheFortifiedPopulationMinimum() {
        final var saved = fixture(true).save();
        final var colony = saved.getColonies().get(0);
        final var stockade = new ConstructionPo();
        stockade.setType(ConstructionType.STOCKADE);
        colony.getConstructions().add(stockade);
        saved.getUnits()
                .removeIf(unit -> unit.getId() == 4 || unit.getId() == 5);
        colony.getConstructions().stream()
                .flatMap(building -> building.getSlots().stream())
                .filter(slot -> Integer.valueOf(4).equals(slot.getWorkerId())
                        || Integer.valueOf(5).equals(slot.getWorkerId()))
                .forEach(slot -> slot.setWorkerId(null));
        final Model model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit fisherman = model.getUnitById(0);
        final String before = new Gson().toJson(model.save());
        assertFalse(fisherman.canLeaveColony());
        assertThrows(IllegalStateException.class, fisherman::cancelColonyWork);
        assertEquals(before, new Gson().toJson(model.save()));
    }
}
