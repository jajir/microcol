package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

/** Owner updates are the only authority for transfer and repair progress. */
class ShipRepairTest {
    private GameModelPo scenario() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        ship.setPlaceEuropePort(null);
        final PlaceMapPo map = new PlaceMapPo();
        map.setLocation(Location.of(26, 14));
        map.setOrientation(Direction.east);
        ship.setPlaceMap(map);
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Ship ship(final Model model) {
        return (Ship) model.getUnitById(3);
    }

    private void complete(final GameModelPo data, final ConstructionType type) {
        final ConstructionPo facility = new ConstructionPo();
        facility.setType(type);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(facility);
    }

    private void ownerUpdate(final Model model) {
        model.endTurn();
        assertEquals("Dutch's King", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    @Test
    void drydockTransferAndThreeRepairsPreserveCargoAcrossSave() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        Model model = load(data);
        model.startGame();
        final Ship ship = ship(model);
        final int passengerId = ship.getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId();
        assertEquals(0, model.planShipRepair(ship).orElseThrow().getColonyId());
        model.damageShip(ship);
        assertTrue(ship.isDamaged());
        assertEquals(4,
                ship.getRepair().orElseThrow().getRemainingOwnerUpdates());
        assertEquals(0, ship.getActionPoints());
        assertThrows(IllegalStateException.class,
                () -> ship.placeToLocation(Location.of(26, 15)));
        assertEquals(passengerId, ship.getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId());
        model = loadSaved(model);
        assertTrue(ship(model).isDamaged());
        ownerUpdate(model);
        assertEquals(PlaceShipRepair.Phase.REPAIR,
                ship(model).getRepair().orElseThrow().getPhase());
        assertEquals(3, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        ownerUpdate(model);
        ownerUpdate(model);
        assertTrue(ship(model).isDamaged());
        ownerUpdate(model);
        assertFalse(ship(model).isDamaged());
        assertEquals(Location.of(25, 14), ship(model).getLocation());
        assertEquals(passengerId, ship(model).getCargo().getSlotByIndex(0)
                .getUnit().orElseThrow().getId());
    }

    @Test
    void shipyardCompletesAfterTransferAndTwoRepairUpdates() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.SHIPYARD);
        final Model model = load(data);
        model.startGame();
        assertEquals(1, model.planShipRepair(ship(model)).orElseThrow()
                .getSeaDistance());
        model.damageShip(ship(model));
        assertEquals(3, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        ownerUpdate(model);
        assertEquals(2, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        ownerUpdate(model);
        assertTrue(ship(model).isDamaged());
        ownerUpdate(model);
        assertFalse(ship(model).isDamaged());
    }

    @Test
    void shipAlreadyInItsOwnYardHasZeroSeaDistance() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceMap()
                .setLocation(Location.of(25, 14));
        final Model model = load(data);
        assertEquals(0, model.planShipRepair(ship(model)).orElseThrow()
                .getSeaDistance());
    }

    @Test
    void lostYardDuringRepairResetsTransferAndFullInterval() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        Model model = load(data);
        model.startGame();
        model.damageShip(ship(model));
        ownerUpdate(model);
        ownerUpdate(model);
        assertEquals(2, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        final var saved = model.save();
        saved.getColonies().stream().filter(colony -> colony.getId() == 0)
                .findFirst().orElseThrow().getConstructions()
                .removeIf(construction -> construction
                        .getType() == ConstructionType.DRYDOCK);
        model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        ownerUpdate(model);
        assertTrue(ship(model).getRepair().orElseThrow().isEuropeDestination());
        assertEquals(PlaceShipRepair.Phase.TRANSFER,
                ship(model).getRepair().orElseThrow().getPhase());
        assertEquals(3, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
    }

    @Test
    void malformedMixedPlaceAndZeroIdEventAreExplicit() {
        final Model model = load(scenario());
        model.startGame();
        model.damageShip(ship(model));
        final var invalid = model.save();
        invalid.getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().setPlaceMap(new PlaceMapPo());
        assertThrows(IllegalArgumentException.class, () -> Model.make(invalid,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final var invalidOrigin = model.save();
        final var repair = invalidOrigin.getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow()
                .getPlaceShipRepair();
        repair.setOrigin(Location.of(25, 14));
        repair.setOriginWasPort(false);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(invalidOrigin,
                        MissionImpl.getPredefinedGameOverEvaluators()));
        final var event = new org.microcol.model.turnevent.TurnEventShipRepair(
                "Dutch",
                org.microcol.model.turnevent.TurnEventShipRepair.Kind.SUNK, 0,
                UnitType.GALLEON, "Europe", true, 0, List.of());
        assertEquals(0, org.microcol.model.turnevent.TurnEventShipRepair
                .tryLoad(event.save()).getShipId());
    }

    @Test
    void formerPortOriginSurvivesColonyRemovalAndReroutes() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceMap()
                .setLocation(Location.of(25, 14));
        final Model model = load(data);
        model.startGame();
        model.damageShip(ship(model));
        final var saved = model.save();
        final var removed = saved.getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        final java.util.Set<Integer> residents = new java.util.HashSet<>();
        removed.getColonyFields().stream().map(field -> field.getWorkerId())
                .filter(java.util.Objects::nonNull).forEach(residents::add);
        removed.getConstructions().stream()
                .flatMap(construction -> construction.getSlots().stream())
                .map(slot -> slot.getWorkerId())
                .filter(java.util.Objects::nonNull).forEach(residents::add);
        saved.getUnits().removeIf(unit -> residents.contains(unit.getId()));
        saved.getColonies().remove(removed);
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertTrue(ship(restored).getRepair().orElseThrow().wasOriginPort());
        ownerUpdate(restored);
        assertTrue(
                ship(restored).getRepair().orElseThrow().isEuropeDestination());
    }

    @Test
    void equalSeaDistanceUsesColonyIdAndBlockedWaterChangesRoute() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        final ConstructionPo second = new ConstructionPo();
        second.setType(ConstructionType.DRYDOCK);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 1).findFirst().orElseThrow()
                .getConstructions().add(second);
        final var terrain = data.getModel().getMap().getTerrainMap();
        for (final Location sea : List.of(Location.of(23, 14),
                Location.of(24, 14), Location.of(23, 13))) {
            terrain.put(sea, TerrainType.OCEAN);
        }
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(java.util.Collections.emptySet());
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceMap()
                .setLocation(Location.of(23, 14));
        final Model tie = load(data);
        assertEquals(0,
                tie.planShipRepair(ship(tie)).orElseThrow().getColonyId());
        final var blocked = data.getModel().getMap().getTerrainMap();
        blocked.put(Location.of(24, 14), TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(blocked);
        final Model detour = load(data);
        assertEquals(1, detour.planShipRepair(ship(detour)).orElseThrow()
                .getColonyId());
    }

    @Test
    void EuropeRepairTakesTransferPlusTwoOwnerUpdates() {
        Model model = load(scenario());
        model.startGame();
        model.damageShip(ship(model));
        assertTrue(ship(model).getRepair().orElseThrow().isEuropeDestination());
        ownerUpdate(model);
        assertEquals(PlaceShipRepair.Phase.REPAIR,
                ship(model).getRepair().orElseThrow().getPhase());
        assertEquals(2, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        model = loadSaved(model);
        assertEquals(2, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
        ownerUpdate(model);
        assertTrue(ship(model).isDamaged());
        ownerUpdate(model);
        assertTrue(ship(model).isAtEuropePort());
        assertFalse(ship(model).isDamaged());
        assertEquals(ship(model).getSpeed(), ship(model).getActionPoints());
    }

    @Test
    void noRepairEndpointSinksPayloadWithSavedNotice() {
        final GameModelPo data = scenario();
        RebellionFixtures.declare(data.getModel(),
                data.getModel().getGameManager().getPlayers().stream()
                        .filter(player -> "Dutch".equals(player.getName()))
                        .findFirst().orElseThrow());
        final Model model = load(data);
        model.startGame();
        final Ship ship = ship(model);
        final int passenger = ship.getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId();
        model.damageShip(ship);
        assertTrue(model.tryGetUnitById(3).isEmpty());
        assertTrue(model.tryGetUnitById(passenger).isEmpty());
        final var notices = model.getTurnEventStore()
                .getForPlayer(model.getCurrentPlayer());
        final var notice = notices.stream().filter(
                event -> event instanceof org.microcol.model.turnevent.TurnEventShipRepair)
                .map(event -> (org.microcol.model.turnevent.TurnEventShipRepair) event)
                .findFirst().orElseThrow();
        assertEquals(org.microcol.model.turnevent.TurnEventShipRepair.Kind.SUNK,
                notice.getKind());
        assertTrue(notice.getLostPassengers() > 0);
        assertEquals(notice.getLostPassengers(),
                ((org.microcol.model.turnevent.TurnEventShipRepair) Model
                        .make(model.save(),
                                MissionImpl.getPredefinedGameOverEvaluators())
                        .getTurnEventStore()
                        .getForPlayer(model.getCurrentPlayer()).stream()
                        .filter(event -> event instanceof org.microcol.model.turnevent.TurnEventShipRepair)
                        .findFirst().orElseThrow()).getLostPassengers());
    }

    @Test
    void lostYardReroutesFromOriginalOriginWithoutSpendingUpdate() {
        final GameModelPo data = scenario();
        complete(data, ConstructionType.DRYDOCK);
        Model model = load(data);
        model.startGame();
        model.damageShip(ship(model));
        final var saved = model.save();
        saved.getColonies().stream().filter(colony -> colony.getId() == 0)
                .findFirst().orElseThrow().getConstructions()
                .removeIf(construction -> construction
                        .getType() == ConstructionType.DRYDOCK);
        model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        assertEquals(0,
                ship(model).getRepair().orElseThrow().getDestinationColonyId());
        ownerUpdate(model);
        assertTrue(ship(model).getRepair().orElseThrow().isEuropeDestination());
        assertEquals(PlaceShipRepair.Phase.TRANSFER,
                ship(model).getRepair().orElseThrow().getPhase());
        assertEquals(3, ship(model).getRepair().orElseThrow()
                .getRemainingOwnerUpdates());
    }

    @Test
    void damagedCargoAndDirectMovementRejectWithoutMutation() {
        final Model model = load(scenario());
        model.startGame();
        final Ship ship = ship(model);
        model.damageShip(ship);
        final Unit passenger = model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
        final String before = new com.google.gson.Gson().toJson(model.save());
        assertFalse(ship.canAttack());
        assertFalse(ship.isPossibleToMoveAt(Location.of(27, 14)));
        assertThrows(IllegalStateException.class, () -> ship.getCargo()
                .getSlotByIndex(4).removeCargo(Goods.of(GoodsType.ORE, 1)));
        assertThrows(IllegalStateException.class, () -> ship.getCargo()
                .getSlotByIndex(4).sellAndEmpty(Goods.of(GoodsType.ORE, 1)));
        assertThrows(IllegalStateException.class,
                () -> ship.placeToEuropePort(model.getEurope().getPort()));
        assertThrows(IllegalStateException.class,
                () -> ship.getCargo().getSlotByIndex(1).storeFromCargoSlot(
                        Goods.of(GoodsType.ORE, 1),
                        ship.getCargo().getSlotByIndex(4)));
        assertThrows(IllegalStateException.class,
                () -> ship.getCargo().getSlotByIndex(1)
                        .storeFromColonyWarehouse(Goods.of(GoodsType.CORN, 1),
                                model.getColonyById(0).orElseThrow()));
        assertFalse(passenger.canEmbark(ship.getCargo().getSlotByIndex(1)));
        assertEquals(before, new com.google.gson.Gson().toJson(model.save()));
    }

    private Model loadSaved(final Model model) {
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        return restored;
    }

    @Test
    void EuropeFallbackAndMalformedRepairPlace() {
        final Model model = load(scenario());
        model.startGame();
        final Ship ship = ship(model);
        assertTrue(model.planShipRepair(ship).orElseThrow().isEurope());
        model.damageShip(ship);
        final UnitPo saved = model.save().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        assertNotNull(saved.getPlaceShipRepair());
        final var invalid = model.save();
        invalid.getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceShipRepair()
                .setRemainingPhaseUpdates(0);
        assertThrows(IllegalArgumentException.class, () -> Model.make(invalid,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
