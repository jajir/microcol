package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlaceShipRepairPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

/**
 * R-0187: declaring independence closes home Europe. A ship sailing there turns
 * back with its cargo, a ship in port is Crown-seized, and a refused
 * declaration changes no voyage or port state.
 */
class HomeEuropeClosureTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Colony 0 of the scenario, a Dutch coastal colony after declarable(). */
    private static final Location PORT = Location.of(25, 14);

    /** Sea-lane square where the scenario's Atlantic voyages depart. */
    private static final Location DEPARTURE = Location.of(2, 15);

    private static final int SHIP_ID = 3;

    private static final int OUTBOUND_ID = 200;

    private static final int PIER_ID = 300;

    /**
     * A Dutch nation with full national support and an owned coastal colony,
     * which satisfies every independence rule except the declaration itself.
     */
    private GameModelPo declarable() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().getCalendar().setEndYear(1900);
        final Location water = PORT.add(Direction.north.getVector());
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(water, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        trees.remove(water);
        data.getModel().getMap().setTrees(trees);
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        fields.remove(water);
        data.getModel().getMap().setFields(fields);
        final Model probe = load(data);
        data.getModel().getColonies()
                .forEach(colony -> colony
                        .setLiberty(200L * probe.getColonyById(colony.getId())
                                .orElseThrow().getPopulation()));
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private UnitPo savedShip(final GameModelPo data) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
    }

    /**
     * Turn the scenario ship into a merchantman on a four-update voyage to
     * Europe with one update spent. The factors are those of the duration-four
     * fixture in VoyageReversalTest, so the saved duration is consistent.
     */
    private void inboundToEurope(final GameModelPo data) {
        final UnitPo ship = savedShip(data);
        ship.setType(UnitType.MERCHANTMAN);
        ship.setAvailableMoves(5);
        ship.getCargo().getSlots().remove(4);
        data.getModel().getUnits().removeIf(unit -> unit.getId() == 13);
        final var goods = ship.getCargo().getSlots().get(3);
        goods.setUnitId(null);
        goods.setGoodsType(GoodsType.ORE);
        goods.setAmount(7);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 16)
                .findFirst().orElseThrow().setTools(80);
        final PlaceHighSeasPo voyage = ship.getPlaceHighSeas();
        voyage.setWeather(1);
        voyage.setOriginalTurns(4);
        voyage.setRemainigTurns(3);
        voyage.setPosition(1);
        voyage.setOriginalToEurope(true);
        voyage.setTravelToEurope(true);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(DEPARTURE, TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        ship.setDepartureLocation(DEPARTURE);
        ship.setDepartureDirection(Direction.west);
    }

    /**
     * Add a second ship that sails from Europe towards the colonies with one
     * update spent (position one, remaining time unchanged in the saved file).
     */
    private void addOutboundShip(final Model model) {
        final int weather = 0;
        final int turns = AtlanticVoyageDuration.calculate(weather, false,
                Direction.west, UnitType.GALLEON.getSpeed(), false);
        final PlaceHighSeasPo voyage = new PlaceHighSeasPo();
        voyage.setWeather(weather);
        voyage.setOriginalTurns(turns);
        voyage.setRemainigTurns(turns);
        voyage.setPosition(0);
        voyage.setOriginalToEurope(false);
        voyage.setTravelToEurope(false);
        final UnitPo outbound = new UnitPo();
        outbound.setId(OUTBOUND_ID);
        outbound.setOwnerId("Dutch");
        outbound.setType(UnitType.GALLEON);
        outbound.setAction(new UnitActionNoActionPo());
        outbound.setAvailableMoves(6);
        outbound.setPlaceHighSeas(voyage);
        outbound.setDepartureLocation(DEPARTURE);
        outbound.setDepartureDirection(Direction.west);
        model.createUnit(model.save(), outbound);
    }

    @Test
    void declarationTurnsInboundShipBackWithItsCargo() {
        final GameModelPo data = declarable();
        inboundToEurope(data);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship inbound = (Ship) model.getUnitById(SHIP_ID);
        assertSame(dutch, inbound.getOwner());
        final int cargo = inbound.getCargo().getSlots().size();

        dutch.declareIndependence();

        final PlaceHighSea sailing = (PlaceHighSea) inbound.getPlace();
        assertFalse(sailing.isTravelToEurope());
        assertEquals(1, sailing.getRemainigTurns());
        assertFalse(inbound.isCrownSeized());
        assertEquals(cargo, inbound.getCargo().getSlots().size());
    }

    @Test
    void inboundShipThatJustDepartedReturnsToItsCoastAtTheDeclaration() {
        final GameModelPo data = declarable();
        inboundToEurope(data);
        // Position zero: the ship has not spent an update at sea yet.
        final PlaceHighSeasPo voyage = savedShip(data).getPlaceHighSeas();
        voyage.setPosition(0);
        voyage.setRemainigTurns(4);
        final Model model = load(data);
        final Ship inbound = (Ship) model.getUnitById(SHIP_ID);
        final List<Integer> passengers = passengersOf(model);

        model.getPlayerByName("Dutch").declareIndependence();

        assertTrue(inbound.isAtPlaceLocation());
        assertEquals(DEPARTURE, inbound.getLocation());
        assertFalse(inbound.isCrownSeized());
        assertEquals(passengers, passengersOf(model));
        assertEquals(7, inbound.getCargo().getSlotByIndex(3).getGoods()
                .orElseThrow().getAmount());
    }

    @Test
    void mixedVoyagesReverseOnlyTheInboundShip() {
        final GameModelPo data = declarable();
        inboundToEurope(data);
        final Model model = load(data);
        addOutboundShip(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship outbound = (Ship) model.getUnitById(OUTBOUND_ID);
        final int outboundTurns = ((PlaceHighSea) outbound.getPlace())
                .getRemainigTurns();

        dutch.declareIndependence();

        final PlaceHighSea inbound = (PlaceHighSea) model.getUnitById(SHIP_ID)
                .getPlace();
        assertFalse(inbound.isTravelToEurope());
        assertEquals(1, inbound.getRemainigTurns());
        final PlaceHighSea sailing = (PlaceHighSea) outbound.getPlace();
        assertFalse(sailing.isTravelToEurope());
        assertEquals(outboundTurns, sailing.getRemainigTurns());
        assertFalse(outbound.isCrownSeized());
    }

    @Test
    void refusedDeclarationChangesNoVoyageOrPortState() {
        final GameModelPo data = declarable();
        inboundToEurope(data);
        data.getModel().getColonies().forEach(colony -> colony.setLiberty(0L));
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship inbound = (Ship) model.getUnitById(SHIP_ID);
        final String before = new com.google.gson.Gson().toJson(model.save());

        assertThrows(IllegalStateException.class, dutch::declareIndependence);

        assertFalse(dutch.isDeclaredIndependence());
        assertFalse(inbound.isCrownSeized());
        assertTrue(((PlaceHighSea) inbound.getPlace()).isTravelToEurope());
        assertEquals(before, new com.google.gson.Gson().toJson(model.save()));
    }

    @Test
    void declarationSeizesShipInPortAndSurvivesSaveWithoutReopeningPort() {
        final GameModelPo data = declarable();
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship ship = (Ship) model.getUnitById(SHIP_ID);
        ship.placeToEuropePort(model.getEurope().getPort());
        assertFalse(ship.isCrownSeized());

        dutch.declareIndependence();

        assertTrue(ship.isCrownSeized());
        assertTrue(ship.isAtEuropePort());
        assertThrows(IllegalStateException.class,
                () -> ship.placeToHighSeas(false));

        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Ship restored = (Ship) reloaded.getUnitById(SHIP_ID);
        assertTrue(restored.isCrownSeized());
        assertTrue(restored.isAtEuropePort());
        assertThrows(IllegalStateException.class,
                () -> restored.placeToHighSeas(false));
    }

    /**
     * The cargo of the duration-four fixture, in the port instead of at sea.
     */
    private void cargoShipInPort(final GameModelPo data) {
        inboundToEurope(data);
        final UnitPo ship = savedShip(data);
        ship.setPlaceHighSeas(null);
        ship.setPlaceEuropePort(new PlaceEuropePortPo());
        final UnitPo pier = new UnitPo();
        pier.setId(PIER_ID);
        pier.setType(UnitType.COLONIST);
        pier.setOwnerId("Dutch");
        pier.setAction(new UnitActionNoActionPo());
        pier.setAvailableMoves(1);
        pier.setWorkUpdates(0);
        pier.setMissionary(false);
        pier.setPlaceEuropePort(new PlaceEuropePortPo());
        data.getModel().getUnits().add(pier);
        ClosedEuropeFixtures.seizeHomeEurope(data.getModel());
    }

    @Test
    void seizedPortAssetsKeepIdentityAndCargoButAreInaccessible() {
        final GameModelPo data = declarable();
        cargoShipInPort(data);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship ship = (Ship) model.getUnitById(SHIP_ID);
        final Unit pier = model.getUnitById(PIER_ID);
        final List<Integer> passengers = new ArrayList<>();
        ship.getCargo().getSlots().forEach(slot -> slot.getUnit()
                .ifPresent(unit -> passengers.add(unit.getId())));
        assertFalse(passengers.isEmpty());
        // Before the declaration the pier unit is an ordinary, usable unit.
        assertFalse(pier.isCrownSeized());
        assertTrue(model.canSetSentry(pier));

        dutch.declareIndependence();

        // Identity and cargo are preserved, the assets are only closed.
        assertSame(ship, model.getUnitById(SHIP_ID));
        assertTrue(ship.isCrownSeized());
        assertTrue(pier.isCrownSeized());
        assertSame(dutch, ship.getOwner());
        final List<Integer> after = new ArrayList<>();
        ship.getCargo().getSlots()
                .forEach(slot -> slot.getUnit().ifPresent(unit -> {
                    after.add(unit.getId());
                    assertTrue(unit.isCrownSeized());
                    assertFalse(unit.canDisembarkToEuropePier());
                }));
        assertEquals(passengers, after);
        final CargoSlot goods = ship.getCargo().getSlotByIndex(3);
        assertEquals(7, goods.getGoods().orElseThrow().getAmount());

        // Every port command is refused and changes nothing.
        final String before = new com.google.gson.Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> model.sellGoods(goods, Goods.of(GoodsType.ORE, 7)));
        assertThrows(IllegalStateException.class,
                () -> dutch.buy(UnitType.MASTER_BLACKSMITH));
        assertThrows(IllegalStateException.class,
                () -> ship.placeToHighSeas(false));
        assertEquals(Optional.of(DisbandProblem.UNAVAILABLE),
                model.getDisbandProblem(pier));
        assertEquals(Optional.of(DisbandProblem.UNAVAILABLE),
                model.getDisbandProblem(ship));
        assertFalse(model.canSetSentry(pier));
        assertEquals(before, new com.google.gson.Gson().toJson(model.save()));

        // Save and load keep the closure and the seized identities.
        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(reloaded.getUnitById(SHIP_ID).isCrownSeized());
        assertTrue(reloaded.getUnitById(PIER_ID).isCrownSeized());
        assertEquals(passengers, passengersOf(reloaded));
    }

    private List<Integer> passengersOf(final Model model) {
        final List<Integer> out = new ArrayList<>();
        ((Ship) model.getUnitById(SHIP_ID)).getCargo().getSlots()
                .forEach(slot -> slot.getUnit()
                        .ifPresent(unit -> out.add(unit.getId())));
        return out;
    }

    @Test
    void declaredNationCannotTurnAnOutboundShipTowardClosedEurope() {
        final GameModelPo data = declarable();
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Ship outbound = (Ship) model.getUnitById(SHIP_ID);
        assertFalse(((PlaceHighSea) outbound.getPlace()).isTravelToEurope());
        // Before the declaration the reverse course toward Europe is offered.
        final VoyageReversalOffer open = model.offerVoyageReversal(outbound);
        assertTrue(open.isNewDestinationEurope());

        dutch.declareIndependence();

        assertThrows(IllegalStateException.class,
                () -> model.offerVoyageReversal(outbound));
        // An offer made earlier is stale and changes nothing.
        final PlaceHighSea voyage = (PlaceHighSea) outbound.getPlace();
        assertThrows(IllegalStateException.class,
                () -> model.reverseVoyage(open));
        assertSame(voyage, outbound.getPlace());
        assertFalse(voyage.isTravelToEurope());
    }

    @Test
    void strictLoadRejectsIncoherentSeizureAfterCarrierBindings() {
        final GameModelPo data = declarable();
        cargoShipInPort(data);
        final Model model = load(data);
        model.getPlayerByName("Dutch").declareIndependence();

        // A declared nation's ship in the port must be seized.
        final ModelPo unseized = model.save();
        unit(unseized, SHIP_ID).setCrownSeized(false);
        assertThrows(IllegalArgumentException.class, () -> Model.make(unseized,
                MissionImpl.getPredefinedGameOverEvaluators()));

        // A passenger aboard that ship must be seized with it.
        final ModelPo passenger = model.save();
        final int passengerId = passengersOf(model).get(0);
        unit(passenger, passengerId).setCrownSeized(false);
        assertThrows(IllegalArgumentException.class, () -> Model.make(passenger,
                MissionImpl.getPredefinedGameOverEvaluators()));

        // A unit on the map is never seized.
        final ModelPo onMap = model.save();
        onMap.getUnits().stream()
                .filter(unit -> "Dutch".equals(unit.getOwnerId())
                        && unit.getPlaceEuropePort() == null
                        && unit.getPlaceCargoSlot() == null)
                .findFirst().orElseThrow().setCrownSeized(true);
        assertThrows(IllegalArgumentException.class, () -> Model.make(onMap,
                MissionImpl.getPredefinedGameOverEvaluators()));

        // Only a declared nation has seized units.
        final ModelPo undeclared = model.save();
        RebellionFixtures.undeclare(undeclared.getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(undeclared,
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

    private static UnitPo unit(final ModelPo model, final int id) {
        return model.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    @Test
    void repairBoundForEuropeFinishesThenSeizesShipAndPassengersInPlace() {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        final UnitPo saved = savedShip(data);
        // The authored galleon is in the port; send it into its last repair
        // update toward Europe from a sea square.
        saved.setPlaceEuropePort(null);
        saved.setMovementThirds(0);
        final PlaceShipRepairPo repair = new PlaceShipRepairPo();
        repair.setOrigin(Location.of(26, 14));
        repair.setOriginWasPort(false);
        repair.setEuropeDestination(true);
        repair.setDestinationColonyId(null);
        repair.setPhase(PlaceShipRepair.Phase.REPAIR.name());
        repair.setRemainingPhaseUpdates(1);
        repair.setRepairDuration(2);
        saved.setPlaceShipRepair(repair);
        ClosedEuropeFixtures.seizeHomeEurope(data.getModel());
        final Model model = load(data);
        model.startGame();
        final Ship ship = (Ship) model.getUnitById(SHIP_ID);
        assertTrue(ship.isDamaged());
        assertFalse(ship.isCrownSeized());
        final List<Integer> passengers = passengersOf(model);
        assertFalse(passengers.isEmpty());

        model.endTurn();
        model.endTurn();

        // The repair finished as before; the arrival is seized, cargo kept.
        assertFalse(ship.isDamaged());
        assertTrue(ship.isAtEuropePort());
        assertTrue(ship.isCrownSeized());
        assertEquals(passengers, passengersOf(model));
        passengers.forEach(
                id -> assertTrue(model.getUnitById(id).isCrownSeized()));
        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(reloaded.getUnitById(SHIP_ID).isCrownSeized());
        assertEquals(passengers, passengersOf(reloaded));
    }

    @Test
    void lateDeliveryCannotPlaceNewUnitsInAClosedPort() {
        final GameModelPo data = declarable();
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        dutch.declareIndependence();
        final int units = model.getAllUnits().size();

        assertThrows(IllegalStateException.class,
                () -> model.addUnitInEurope(UnitType.FRIGATE, dutch));

        assertEquals(units, model.getAllUnits().size());
    }

    @Test
    void refusedDepartureEndsActionAndPreservesShip() {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        final UnitPo saved = savedShip(data);
        saved.setPlaceEuropePort(null);
        final org.microcol.model.store.PlaceMapPo place = new org.microcol.model.store.PlaceMapPo();
        place.setLocation(DEPARTURE);
        place.setOrientation(Direction.west);
        saved.setPlaceMap(place);
        saved.setAvailableMoves(6);
        final Location target = Location.of(1, 15);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(DEPARTURE, TerrainType.HIGH_SEA);
        terrain.put(target, TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        ClosedEuropeFixtures.seizeHomeEurope(data.getModel());
        final Model model = load(data);
        model.startGame();
        final Unit ship = model.getUnitById(SHIP_ID);
        final int moves = ship.getMovementThirds();
        final List<String> actions = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMoveStarted(
                    final org.microcol.model.event.UnitMoveStartedEvent event) {
                actions.add("movement started");
            }

            @Override
            public void onActionStarted(
                    final org.microcol.model.event.ActionStartedEvent event) {
                actions.add("started");
            }

            @Override
            public void onActionEnded(
                    final org.microcol.model.event.ActionEndedEvent event) {
                actions.add("ended");
            }
        });
        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(ship, Path.of(List.of(target))));
        assertEquals(List.of("started", "ended"), actions);
        assertEquals(DEPARTURE, ship.getLocation());
        assertEquals(moves, ship.getMovementThirds());
        assertFalse(ship.isCrownSeized());
    }

}
