package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.FleetReport.Destination;
import org.microcol.model.FleetReport.ForeignShip;
import org.microcol.model.FleetReport.OwnShip;
import org.microcol.model.LaborLocation.Kind;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlaceShipRepairPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.gson.Gson;

/**
 * Fleet report (R-0202): every own ship of a colonial nation with its real
 * place, voyage, repair, passengers, goods and holds, and the foreign ships in
 * present sight with only their occupied-hold count. Every world is saved
 * through the ordinary PO form, and boarding goes through the model's embark
 * operation, so the loader's invariants hold for each saved state.
 */
class FleetReportTest {

    private static final String MISSION = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH = "Dutch";

    private static final String FRENCH = "France";

    /** Ocean square of the mission map where the Atlantic voyages depart. */
    private static final Location DEPARTURE = Location.of(2, 15);

    /** Ocean square of the island world, next to the Dutch colonist. */
    private static final Location SEA = NativeTurnScene.at(5, 0);

    private static final Location LAND = NativeTurnScene.at(4, 0);

    /**
     * Ocean beyond the island, far from the Dutch colonist's sight and from
     * every saved Dutch unit.
     */
    private static final Location FAR = NativeTurnScene.at(-6, -6);

    /** The Dutch galleon that the free-play map already saves, at sea. */
    private static final int SAVED_GALLEON_ID = 3;

    private static final int HOLDS = 6;

    /** IDs above the saved units of the scenarios, so none is reused. */
    private static final int OWN_ID = 9001;

    private static final int COLONIST_ID = 9002;

    private static final int TREASURE_ID = 9003;

    private static final int FOREIGN_ID = 9004;

    private static final int PORT_FAMILY_ID = 9010;

    private static final int VOYAGE_TO_EUROPE_ID = 9201;

    private static final int REVERSED_ID = 9202;

    private static final int WAITING_ID = 9203;

    private static final int REPAIR_ID = 9301;

    private final Gson gson = new Gson();

    @Test
    void everyShipFamilyIsReportedWithItsOwnHolds() {
        final List<UnitPo> ships = new ArrayList<>();
        for (final UnitType type : families()) {
            ships.add(inPort(PORT_FAMILY_ID + ships.size(), type));
        }
        final Model model = mission(ships);

        final FleetReport report = model.getFleetReport(dutch(model));
        for (int index = 0; index < families().size(); index++) {
            final UnitType type = families().get(index);
            final OwnShip ship = report.findOwnShip(PORT_FAMILY_ID + index)
                    .orElseThrow();
            assertEquals(type, ship.getType());
            assertEquals(type.getCargoCapacity(), ship.getCapacity());
            assertEquals(0, ship.getOccupiedHolds());
            assertEquals(type.getCargoCapacity(), ship.getFreeHolds());
            assertEquals(Kind.EUROPE, ship.getLocation().getKind());
            assertTrue(ship.getVoyage().isEmpty());
            assertTrue(ship.getRepair().isEmpty());
        }
    }

    @Test
    void emptyShipHasEveryHoldFree() {
        final Model model = NativeTurnScene.make(ownWorld());

        final OwnShip ship = own(model);
        assertEquals(0, ship.getPassengers());
        assertTrue(ship.getGoods().isEmpty());
        assertEquals(0, ship.getOccupiedHolds());
        assertEquals(HOLDS, ship.getFreeHolds());
    }

    @Test
    void partialGoodsAreCountedByHoldAndByType() {
        final ModelPo po = ownWorld();
        withGoods(po, 0, GoodsType.CORN, 5);
        withGoods(po, 2, GoodsType.ORE, 3);
        final Model model = NativeTurnScene.make(po);

        final OwnShip ship = own(model);
        assertEquals(0, ship.getPassengers());
        assertEquals(2, ship.getOccupiedHolds());
        assertEquals(HOLDS - 2, ship.getFreeHolds());
        assertEquals(Map.of(GoodsType.CORN, 5, GoodsType.ORE, 3),
                ship.getGoods());
    }

    @Test
    void fullShipHasNoFreeHold() {
        final ModelPo po = ownWorld();
        for (int hold = 0; hold < HOLDS; hold++) {
            withGoods(po, hold, GoodsType.CORN, 1);
        }
        final Model model = NativeTurnScene.make(po);

        final OwnShip ship = own(model);
        assertEquals(HOLDS, ship.getOccupiedHolds());
        assertEquals(0, ship.getFreeHolds());
    }

    @Test
    void passengerOccupiesItsHoldAndIsCountedOnce() {
        final Model model = NativeTurnScene.make(ownWorld());
        colonist(model).embarkFromLocation(
                ownShip(model).getCargo().getSlotByIndex(0));

        final OwnShip ship = own(model);
        assertEquals(1, ship.getPassengers());
        assertEquals(1, ship.getOccupiedHolds());
        assertEquals(HOLDS - 1, ship.getFreeHolds());
        assertTrue(ship.getGoods().isEmpty());
    }

    @Test
    void reservedHoldsOfMultiHoldPassengerAreOccupied() {
        final Model model = NativeTurnScene.make(ownWorld());
        treasure(model).embarkFromLocation(
                ownShip(model).getCargo().getSlotByIndex(0));

        // One treasure train reserves all six holds; it is one passenger.
        final OwnShip ship = own(model);
        assertEquals(1, ship.getPassengers());
        assertEquals(HOLDS, ship.getOccupiedHolds());
        assertEquals(0, ship.getFreeHolds());
    }

    @Test
    void ownShipOnTheMapNamesItsTileAndIsNavigable() {
        final Model model = NativeTurnScene.make(ownWorld());

        final OwnShip ship = own(model);
        assertEquals(Kind.MAP, ship.getLocation().getKind());
        assertEquals(SEA, ship.getLocation().getLocation().orElseThrow());
        assertTrue(ship.getLocation().isNavigable());
        assertTrue(ship.getVoyage().isEmpty());
        assertTrue(ship.getRepair().isEmpty());
    }

    @Test
    void voyageToEuropeReportsDestinationAndTurnsLeft() {
        final int turns = AtlanticVoyageDuration.calculate(0, true,
                Direction.west, UnitType.MERCHANTMAN.getSpeed());
        final Model model = mission(
                List.of(voyaging(VOYAGE_TO_EUROPE_ID, true, true, 1)));

        final OwnShip ship = report(model, VOYAGE_TO_EUROPE_ID);
        final FleetReport.Voyage voyage = ship.getVoyage().orElseThrow();
        assertEquals(Kind.AT_SEA, ship.getLocation().getKind());
        assertFalse(ship.getLocation().isNavigable());
        assertEquals(Destination.EUROPE, voyage.getDestination());
        assertEquals(turns - 1, voyage.getRemainingTurns());
        assertFalse(voyage.isReversed());
        assertFalse(voyage.isWaiting());
    }

    @Test
    void reversedVoyageReportsTheTurnedAroundCourse() {
        final Model model = mission(
                List.of(voyaging(REVERSED_ID, false, true, 1)));

        final FleetReport.Voyage voyage = report(model, REVERSED_ID).getVoyage()
                .orElseThrow();
        assertEquals(Destination.EUROPE, voyage.getDestination());
        assertEquals(1, voyage.getRemainingTurns());
        assertTrue(voyage.isReversed());
        assertFalse(voyage.isWaiting());
    }

    @Test
    void shipWaitingForAFreeSquareInTheColoniesIsNamedAsWaiting() {
        final int turns = AtlanticVoyageDuration.calculate(0, false,
                Direction.west, UnitType.MERCHANTMAN.getSpeed());
        final Model model = mission(
                List.of(voyaging(WAITING_ID, false, false, turns)));

        final FleetReport.Voyage voyage = report(model, WAITING_ID).getVoyage()
                .orElseThrow();
        assertEquals(Destination.COLONIES, voyage.getDestination());
        assertEquals(0, voyage.getRemainingTurns());
        assertTrue(voyage.isWaiting());
    }

    @Test
    void damagedShipReportsItsRemainingUpdatesAndNoDestination() {
        final Model model = mission(List.of(repairing(REPAIR_ID)));

        final OwnShip ship = report(model, REPAIR_ID);
        final FleetReport.Repair repair = ship.getRepair().orElseThrow();
        assertEquals(Kind.REPAIR, ship.getLocation().getKind());
        assertFalse(ship.getLocation().isNavigable());
        assertTrue(ship.getVoyage().isEmpty());
        assertTrue(repair.isEuropeDestination());
        assertTrue(repair.getColonyName().isEmpty());
        // One transfer update, then the two-update repair in Europe.
        assertEquals(1 + 2, repair.getRemainingOwnerUpdates());
    }

    @Test
    void visibleForeignShipDisclosesOnlyItsOccupiedHolds() {
        final ModelPo po = foreignWorld(SEA);
        withForeignGoods(po, 1, GoodsType.ORE, 10);
        withForeignGoods(po, 3, GoodsType.CORN, 2);
        final Model model = NativeTurnScene.make(po);

        final FleetReport report = model.getFleetReport(dutch(model));
        // The French galleon is not an own ship; the only own ship is the one
        // that the free-play map saves.
        assertEquals(Set.of(SAVED_GALLEON_ID), ownIds(report));
        assertTrue(report.findOwnShip(FOREIGN_ID).isEmpty());
        assertEquals(Set.of(FOREIGN_ID), foreignIds(report));
        final ForeignShip foreign = report.findForeignShip(FOREIGN_ID)
                .orElseThrow();
        assertEquals(UnitType.GALLEON, foreign.getType());
        assertEquals(SEA, foreign.getLocation());
        assertEquals(2, foreign.getOccupiedHolds());
    }

    @Test
    void hiddenForeignShipIsAbsentUntilItIsInSight() {
        final Model model = NativeTurnScene.make(foreignWorld(FAR));

        assertTrue(
                model.getFleetReport(dutch(model)).getForeignShips().isEmpty());
        assertTrue(model.getFleetReport(dutch(model))
                .findForeignShip(FOREIGN_ID).isEmpty());

        model.getUnitById(FOREIGN_ID).placeToLocation(SEA);

        assertEquals(0, model.getFleetReport(dutch(model))
                .findForeignShip(FOREIGN_ID).orElseThrow().getOccupiedHolds());
    }

    @Test
    void ownerSwitchMovesTheShipBetweenOwnAndForeignLists() {
        final ModelPo po = foreignWorld(SEA);
        withForeignGoods(po, 0, GoodsType.CORN, 5);
        final Model model = NativeTurnScene.make(po);
        final Ship ship = (Ship) model.getUnitById(FOREIGN_ID);

        // Before the switch the ship is French and only its count is shown.
        final FleetReport before = model.getFleetReport(dutch(model));
        assertEquals(Set.of(SAVED_GALLEON_ID), ownIds(before));
        assertEquals(Set.of(FOREIGN_ID), foreignIds(before));

        ship.takeOver(dutch(model));
        final FleetReport own = model.getFleetReport(dutch(model));
        assertEquals(Set.of(SAVED_GALLEON_ID, FOREIGN_ID), ownIds(own));
        assertEquals(1,
                own.findOwnShip(FOREIGN_ID).orElseThrow().getOccupiedHolds());
        assertTrue(own.getForeignShips().isEmpty());

        ship.takeOver(france(model));
        final FleetReport foreign = model.getFleetReport(dutch(model));
        assertEquals(Set.of(SAVED_GALLEON_ID), ownIds(foreign));
        assertTrue(foreign.findOwnShip(FOREIGN_ID).isEmpty());
        assertEquals(Set.of(FOREIGN_ID), foreignIds(foreign));
        assertEquals(1, foreign.findForeignShip(FOREIGN_ID).orElseThrow()
                .getOccupiedHolds());
    }

    @Test
    void savedFreePlayGalleonIsReportedWithItsRealPassengers() {
        final Model model = NativeTurnScene.make(foreignWorld(SEA));

        // The map saves three free colonists aboard the Dutch galleon.
        final OwnShip saved = report(model, SAVED_GALLEON_ID);
        assertEquals(UnitType.GALLEON, saved.getType());
        assertEquals(Kind.MAP, saved.getLocation().getKind());
        assertEquals(Map.of(UnitType.COLONIST, 3), saved.getPassengersByType());
        assertEquals(3, saved.getPassengers());
        assertEquals(3, saved.getOccupiedHolds());
        assertTrue(saved.getGoods().isEmpty());
    }

    @Test
    void readingNeverChangesTheSavedGameAndReloadKeepsTheFigures() {
        final ModelPo po = ownWorld();
        withGoods(po, 0, GoodsType.CORN, 5);
        final Model model = NativeTurnScene.make(po);
        colonist(model).embarkFromLocation(
                ownShip(model).getCargo().getSlotByIndex(1));
        final String before = gson.toJson(model.save());
        final int movesBefore = model.getUnitById(COLONIST_ID)
                .getActionPoints();

        final OwnShip first = own(model);
        final OwnShip second = own(model);

        assertEquals(before, gson.toJson(model.save()));
        assertEquals(movesBefore,
                model.getUnitById(COLONIST_ID).getActionPoints());
        assertEquals(first.getOccupiedHolds(), second.getOccupiedHolds());

        final Model restored = NativeTurnScene.make(model.save());
        final OwnShip reloaded = own(restored);
        assertEquals(first.getOccupiedHolds(), reloaded.getOccupiedHolds());
        assertEquals(first.getPassengers(), reloaded.getPassengers());
        assertEquals(first.getGoods(), reloaded.getGoods());
        assertEquals(SEA, reloaded.getLocation().getLocation().orElseThrow());
    }

    @Test
    void passengersAreNamedByTypeWithTheirCounts() {
        final Model model = NativeTurnScene.make(ownWorld());
        colonist(model).embarkFromLocation(
                ownShip(model).getCargo().getSlotByIndex(0));

        assertEquals(Map.of(UnitType.COLONIST, 1),
                own(model).getPassengersByType());
    }

    @Test
    void treasureTrainIsOneNamedPassengerAndItsReservedHoldsAreOccupied() {
        final Model model = NativeTurnScene.make(ownWorld());
        treasure(model).embarkFromLocation(
                ownShip(model).getCargo().getSlotByIndex(0));

        final OwnShip ship = own(model);
        assertEquals(Map.of(UnitType.TREASURE_TRAIN, 1),
                ship.getPassengersByType());
        assertEquals(1, ship.getPassengers());
        assertEquals(HOLDS, ship.getOccupiedHolds());
    }

    /**
     * The foreign row exposes only identity, type, tile and the count: no
     * getter returns a unit, a player, cargo or goods object of the live model.
     */
    @Test
    void foreignShipReportHoldsNoLiveUnitPlayerOrCargoHandle() {
        for (final Method method : ForeignShip.class.getDeclaredMethods()) {
            assertFalse(isLiveHandle(method.getReturnType()),
                    "Foreign ship getter " + method.getName()
                            + " returns a live handle");
        }
        for (final Field field : ForeignShip.class.getDeclaredFields()) {
            assertFalse(isLiveHandle(field.getType()), "Foreign ship field "
                    + field.getName() + " holds a live handle");
        }
    }

    private static boolean isLiveHandle(final Class<?> type) {
        return Unit.class.isAssignableFrom(type)
                || Player.class.isAssignableFrom(type)
                || Cargo.class.isAssignableFrom(type)
                || CargoSlot.class.isAssignableFrom(type)
                || Goods.class.isAssignableFrom(type);
    }

    @Test
    void identityFromAnotherModelOrNullIsRefused() {
        final Model model = NativeTurnScene.make(ownWorld());
        final Model restored = NativeTurnScene.make(model.save());

        assertThrows(IllegalArgumentException.class,
                () -> model.getFleetReport(dutch(restored)));
        assertThrows(NullPointerException.class,
                () -> model.getFleetReport(null));
    }

    // ----- helpers -----

    private static List<UnitType> families() {
        return List.of(UnitType.FRIGATE, UnitType.GALLEON, UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.PRIVATEER, UnitType.MAN_OF_WAR);
    }

    /** Fields every saved unit carries; a ship adds its own place. */
    private static UnitPo unit(final int id, final String owner,
            final UnitType type) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(owner);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        unit.setCargo(new CargoPo());
        for (int hold = 0; hold < type.getCargoCapacity(); hold++) {
            unit.getCargo().getSlots().add(new CargoSlotPo());
        }
        return unit;
    }

    private static UnitPo inPort(final int id, final UnitType type) {
        final UnitPo ship = unit(id, DUTCH, type);
        ship.setPlaceEuropePort(new PlaceEuropePortPo());
        return ship;
    }

    /**
     * A Dutch ship on a voyage. The duration and remaining turns follow the
     * ordinary rules, so the saved file is consistent.
     */
    private static UnitPo voyaging(final int id, final boolean originalToEurope,
            final boolean travelToEurope, final int position) {
        final int weather = 0;
        final UnitType type = UnitType.MERCHANTMAN;
        final int turns = AtlanticVoyageDuration.calculate(weather,
                originalToEurope, Direction.west, type.getSpeed());
        final PlaceHighSeasPo voyage = new PlaceHighSeasPo();
        voyage.setWeather(weather);
        voyage.setOriginalToEurope(originalToEurope);
        voyage.setTravelToEurope(travelToEurope);
        voyage.setOriginalTurns(turns);
        voyage.setPosition(position);
        voyage.setRemainigTurns(
                travelToEurope == originalToEurope ? turns - position
                        : position);
        final UnitPo ship = unit(id, DUTCH, type);
        ship.setPlaceHighSeas(voyage);
        ship.setDepartureLocation(DEPARTURE);
        ship.setDepartureDirection(Direction.west);
        return ship;
    }

    /** A Dutch ship in a repair that transfers it to Europe. */
    private static UnitPo repairing(final int id) {
        final PlaceShipRepairPo repair = new PlaceShipRepairPo();
        repair.setOrigin(DEPARTURE);
        repair.setOriginWasPort(false);
        repair.setEuropeDestination(true);
        repair.setDestinationColonyId(null);
        repair.setPhase("TRANSFER");
        repair.setRemainingPhaseUpdates(1);
        repair.setRepairDuration(2);
        final UnitPo ship = unit(id, DUTCH, UnitType.MERCHANTMAN);
        // The loader accepts a repairing ship only without movement and
        // without any other place, as in the F54 fixture.
        ship.setMovementThirds(0);
        ship.setPlaceShipRepair(repair);
        return ship;
    }

    /** The mission map with the added units; its departure square is ocean. */
    private static Model mission(final List<UnitPo> added) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath(MISSION);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(DEPARTURE, TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getUnits().addAll(added);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Island world with a Dutch galleon at sea, a Dutch colonist and a Dutch
     * treasure train on land ready to board, and the Dutch player to move.
     */
    private static ModelPo ownWorld() {
        final ModelPo po = NativeTurnScene.island()
                .rawUnit(OWN_ID, UnitType.GALLEON, DUTCH, null, null, SEA)
                .rawUnit(COLONIST_ID, UnitType.COLONIST, DUTCH, null, null,
                        LAND)
                .rawUnit(TREASURE_ID, UnitType.TREASURE_TRAIN, DUTCH, null,
                        null, LAND)
                .dutchColonist(4, 1).po();
        for (int hold = 0; hold < HOLDS; hold++) {
            unitPo(po, OWN_ID).getCargo().getSlots().add(new CargoSlotPo());
        }
        unitPo(po, COLONIST_ID).setMovementThirds(MovementPoints.ofPoints(1));
        final UnitPo train = unitPo(po, TREASURE_ID);
        train.setTreasureValue(500);
        train.setMovementThirds(MovementPoints.ofPoints(1));
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH);
        return po;
    }

    /** Island world with a French galleon at the given square. */
    private static ModelPo foreignWorld(final Location at) {
        final ModelPo po = NativeTurnScene.island()
                .rawUnit(FOREIGN_ID, UnitType.GALLEON, FRENCH, null, null, at)
                .dutchColonist(4, 1).po();
        for (int hold = 0; hold < HOLDS; hold++) {
            unitPo(po, FOREIGN_ID).getCargo().getSlots().add(new CargoSlotPo());
        }
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH);
        return po;
    }

    private static void withGoods(final ModelPo po, final int hold,
            final GoodsType type, final int amount) {
        final CargoSlotPo slot = unitPo(po, OWN_ID).getCargo().getSlots()
                .get(hold);
        slot.setGoodsType(type);
        slot.setAmount(amount);
    }

    private static void withForeignGoods(final ModelPo po, final int hold,
            final GoodsType type, final int amount) {
        final CargoSlotPo slot = unitPo(po, FOREIGN_ID).getCargo().getSlots()
                .get(hold);
        slot.setGoodsType(type);
        slot.setAmount(amount);
    }

    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static Set<Integer> ownIds(final FleetReport report) {
        return report.getOwnShips().stream().map(OwnShip::getUnitId)
                .collect(Collectors.toSet());
    }

    private static Set<Integer> foreignIds(final FleetReport report) {
        return report.getForeignShips().stream().map(ForeignShip::getUnitId)
                .collect(Collectors.toSet());
    }

    private static OwnShip own(final Model model) {
        return report(model, OWN_ID);
    }

    private static OwnShip report(final Model model, final int id) {
        return model.getFleetReport(dutch(model)).findOwnShip(id).orElseThrow();
    }

    private static Ship ownShip(final Model model) {
        return (Ship) model.getUnitById(OWN_ID);
    }

    private static Unit colonist(final Model model) {
        return model.getUnitById(COLONIST_ID);
    }

    private static Unit treasure(final Model model) {
        return model.getUnitById(TREASURE_ID);
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    private static Player france(final Model model) {
        return model.getPlayerByName(FRENCH);
    }
}
