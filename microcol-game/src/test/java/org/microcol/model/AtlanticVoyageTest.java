package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.microcol.model.campaign.MissionImpl;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

/** Actual movement, owner turns and JSON retain one Atlantic voyage. */
class AtlanticVoyageTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();
    private GameModelPo data;
    private Model model;

    private void prepare(boolean west, boolean inEurope) {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (int y = 1; y <= 30; y++) {
            for (int x : new int[] { 1, 2, 49, 50 }) {
                terrain.put(Location.of(x, y), TerrainType.HIGH_SEA);
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
        UnitPo ship = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 3).findFirst().orElseThrow();
        ship.setPlaceHighSeas(null);
        ship.setDepartureLocation(null);
        ship.setDepartureDirection(null);
        if (inEurope) {
            ship.setPlaceEuropePort(new PlaceEuropePortPo());
        } else {
            PlaceMapPo at = new PlaceMapPo();
            at.setLocation(Location.of(west ? 3 : 48, 15));
            at.setOrientation(west ? Direction.west : Direction.east);
            ship.setPlaceMap(at);
        }
        CargoSlotPo ore = ship.getCargo().getSlots().get(4);
        ore.setGoodsType(GoodsType.ORE);
        ore.setAmount(7);
        data.getModel().getUnits().stream().filter(u -> u.getId() == 16)
                .findFirst().orElseThrow().setTools(80);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Ship ship() {
        return (Ship) model.getUnitById(3);
    }

    private void nextOwnerTurn() {
        model.endTurn();
        assertEquals("Dutch's King", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private void reload() {
        data.setModel(model.save());
        Path save = directory.resolve("voyage.microcol");
        dao.saveToFile(save.toString(), data);
        data = dao.loadFromFile(save.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
    }

    private void assertCargo() {
        assertEquals(3, ship().getId());
        assertEquals("Dutch", ship().getOwner().getName());
        assertEquals(16, ship().getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId());
        assertEquals(80, ((org.microcol.model.unit.UnitFreeColonist) model
                .getUnitById(16)).getTools());
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship().getCargo().getSlotByIndex(4).getGoods().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void entryAlongAndInwardStayOnMapThenReturnToExactDeparture(boolean west) {
        prepare(west, false);
        int inner = west ? 2 : 49;
        int ocean = west ? 3 : 48;
        int outer = west ? 1 : 50;
        ship().moveOneStep(Location.of(inner, 15));
        assertEquals(Location.of(inner, 15), ship().getLocation());
        ship().moveOneStep(Location.of(inner, 16));
        assertEquals(Location.of(inner, 16), ship().getLocation());
        ship().moveOneStep(Location.of(ocean, 16));
        assertEquals(Location.of(ocean, 16), ship().getLocation());
        ship().moveOneStep(Location.of(inner, 16));
        int moves = ship().getActionPoints();
        ship().moveOneStep(Location.of(outer, 16));
        assertTrue(ship().isAtHighSea());
        assertEquals(moves - 1, ship().getActionPoints());
        PlaceHighSea outward = (PlaceHighSea) ship().getPlace();
        int duration = outward.getOriginalTurns();
        assertEquals(AtlanticVoyageDuration.calculate(outward.getWeather(),
                true, west ? Direction.west : Direction.east,
                ship().getType().getSpeed()), duration);
        assertEquals(duration, outward.getRemainigTurns());
        assertCargo();
        reload();
        assertEquals(duration,
                ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        assertEquals(outward.getWeather(),
                ((PlaceHighSea) ship().getPlace()).getWeather());
        for (int turn = 0; turn < duration; turn++)
            nextOwnerTurn();
        assertTrue(ship().isAtEuropePort());
        assertCargo();
        reload();
        ship().placeToHighSeas(false);
        PlaceHighSea inbound = (PlaceHighSea) ship().getPlace();
        int returnDuration = inbound.getOriginalTurns();
        assertEquals(AtlanticVoyageDuration.calculate(inbound.getWeather(),
                false, west ? Direction.west : Direction.east,
                ship().getType().getSpeed()), returnDuration);
        reload();
        assertEquals(returnDuration,
                ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        assertEquals(inbound.getWeather(),
                ((PlaceHighSea) ship().getPlace()).getWeather());
        for (int turn = 0; turn < returnDuration; turn++)
            nextOwnerTurn();
        assertEquals(Location.of(inner, 16), ship().getLocation());
        assertEquals(west ? Direction.east : Direction.west,
                ship().save().getPlaceMap().getOrientation());
        assertCargo();
    }

    @Test
    void firstEuropeanDepartureUsesStableEasternProfileAndKeepsCargo() {
        prepare(false, true);
        ship().placeToHighSeas(false);
        int duration = ((PlaceHighSea) ship().getPlace()).getOriginalTurns();
        reload();
        for (int turn = 0; turn < duration; turn++)
            nextOwnerTurn();
        assertEquals(Location.of(49, 15), ship().getLocation());
        assertCargo();
    }

    @Test
    void departureRejectsWrongPlaceOrNoMovesWithoutPartialEffects() {
        prepare(false, false);
        Place before = ship().getPlace();
        int moves = ship().getActionPoints();
        assertThrows(IllegalStateException.class,
                () -> ship().placeToHighSeas(true));
        assertSame(before, ship().getPlace());
        assertEquals(moves, ship().getActionPoints());
        ship().moveOneStep(Location.of(49, 15));
        ship().setActionPoints(0);
        before = ship().getPlace();
        assertThrows(IllegalStateException.class,
                () -> ship().moveOneStep(Location.of(50, 15)));
        assertSame(before, ship().getPlace());
        assertEquals(0, ship().getActionPoints());
        assertCargo();
    }

    private Unit addShip(final String owner, final Location location) {
        UnitPo added = new UnitPo();
        added.setId(100);
        added.setOwnerId(owner);
        added.setType(UnitType.GALLEON);
        added.setAction(
                new org.microcol.model.unit.UnitActionNoAction().save());
        added.setAvailableMoves(6);
        PlaceMapPo place = new PlaceMapPo();
        place.setLocation(location);
        place.setOrientation(Direction.west);
        added.setPlaceMap(place);
        return model.createUnit(model.save(), added);
    }

    @Test
    void occupiedOutwardStepCannotConsumeMovesOrRecordADeparture() {
        prepare(false, false);
        ship().moveOneStep(Location.of(49, 15));
        addShip("Dutch's King", Location.of(50, 15));
        Place before = ship().getPlace();
        int moves = ship().getActionPoints();
        assertThrows(IllegalArgumentException.class,
                () -> ship().moveOneStep(Location.of(50, 15)));
        assertSame(before, ship().getPlace());
        assertEquals(moves, ship().getActionPoints());
        assertTrue(ship().getDepartureLocation().isEmpty());
        assertTrue(ship().getDepartureDirection().isEmpty());
        assertCargo();
    }

    @Test
    void blockedArrivalWaitsAtZeroAcrossSaveThenUsesTheSameSquare() {
        prepare(false, true);
        Location arrival = Location.of(49, 15);
        addShip("Dutch's King", arrival);
        ship().placeToHighSeas(false);
        int duration = ((PlaceHighSea) ship().getPlace()).getOriginalTurns();
        for (int turn = 0; turn < duration; turn++)
            nextOwnerTurn();
        assertTrue(ship().isAtHighSea());
        assertEquals(0, ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        assertCargo();
        reload();
        nextOwnerTurn();
        assertEquals(0, ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        assertCargo();
        model.destroyUnit(model.getUnitById(100));
        nextOwnerTurn();
        assertEquals(arrival, ship().getLocation());
        assertCargo();
    }

    @Test
    void friendlyShipDoesNotRelocateDefaultArrival() {
        prepare(false, true);
        Location arrival = Location.of(49, 15);
        addShip("Dutch", arrival);
        ship().placeToHighSeas(false);
        int duration = ((PlaceHighSea) ship().getPlace()).getOriginalTurns();
        for (int turn = 0; turn < duration; turn++)
            nextOwnerTurn();
        assertEquals(arrival, ship().getLocation());
        assertEquals(2, model.getUnitsAt(arrival).size());
    }

    @Test
    void malformedSavedCoastIsRejectedInsteadOfGuessed() {
        prepare(false, true);
        ship().placeToHighSeas(false);
        data.setModel(model.save());
        UnitPo saved = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 3).findFirst().orElseThrow();
        saved.setDepartureDirection(null);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        saved.setDepartureLocation(null);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        saved.setDepartureLocation(Location.of(49, 15));
        saved.setDepartureDirection(Direction.west);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        saved.setDepartureDirection(Direction.east);
        saved.setDepartureLocation(Location.of(48, 15));
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        saved.setDepartureLocation(Location.of(51, 15));
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        saved.setDepartureLocation(Location.of(49, 15));
        saved.getPlaceHighSeas().setRemainigTurns(-1);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void missingOrInconsistentVoyageFactorsAreRejected() {
        prepare(false, true);
        ship().placeToHighSeas(false);
        data.setModel(model.save());
        final var voyage = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow()
                .getPlaceHighSeas();
        final int weather = voyage.getWeather();
        final int total = voyage.getOriginalTurns();
        voyage.setWeather(null);
        assertInvalidVoyage();
        voyage.setWeather(2);
        assertInvalidVoyage();
        voyage.setWeather(weather);
        voyage.setOriginalTurns(null);
        assertInvalidVoyage();
        voyage.setOriginalTurns(5);
        assertInvalidVoyage();
        voyage.setOriginalTurns(total);
        voyage.setRemainigTurns(total + 1);
        assertInvalidVoyage();
        voyage.setRemainigTurns(total);
        voyage.setPosition(0);
        voyage.setOriginalTurns(total == 4 ? 3 : total + 1);
        assertInvalidVoyage();
    }

    private void assertInvalidVoyage() {
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void savedMidVoyageRetainsWeatherOriginalAndRemainingUntilArrival() {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final PlaceHighSea departure = (PlaceHighSea) ship().getPlace();
        assertEquals(1, departure.getWeather());
        assertEquals(3, departure.getOriginalTurns());
        assertEquals(3, departure.getRemainigTurns());
        nextOwnerTurn();
        assertEquals(2, ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        reload();
        final PlaceHighSea resumed = (PlaceHighSea) ship().getPlace();
        assertEquals(1, resumed.getWeather());
        assertEquals(3, resumed.getOriginalTurns());
        assertEquals(2, resumed.getRemainigTurns());
        nextOwnerTurn();
        assertEquals(1, ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
        nextOwnerTurn();
        assertEquals(Location.of(49, 15), ship().getLocation());
    }

    @Test
    void missingDefaultDestinationRejectsBeforeLeavingEurope() {
        prepare(false, true);
        data.setModel(model.save());
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.entrySet().removeIf(e -> e.getValue() == TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        Place before = ship().getPlace();
        assertThrows(IllegalStateException.class,
                () -> ship().placeToHighSeas(false));
        assertSame(before, ship().getPlace());
        assertTrue(ship().getDepartureLocation().isEmpty());
        assertCargo();
    }

    @Test
    void wrongTurnAndRepeatedDepartureCannotRestartVoyage() {
        prepare(false, true);
        model.endTurn();
        assertThrows(IllegalStateException.class,
                () -> ship().placeToHighSeas(false));
        assertTrue(ship().isAtEuropePort());
        model.endTurn();
        ship().placeToHighSeas(false);
        Place voyage = ship().getPlace();
        assertThrows(IllegalStateException.class,
                () -> ship().placeToHighSeas(false));
        assertSame(voyage, ship().getPlace());
        assertEquals(((PlaceHighSea) voyage).getOriginalTurns(),
                ((PlaceHighSea) voyage).getRemainigTurns());
        assertCargo();
    }

    @Test
    void newlyCreatedRoyalShipHasASavableArrivalDestination() {
        prepare(false, true);
        Ship royal = (Ship) model
                .createCargoShipForKing(model.getPlayerByName("Dutch's King"));
        assertEquals(Location.of(49, 15),
                royal.getDepartureLocation().orElseThrow());
        int id = royal.getId();
        reload();
        Ship restored = (Ship) model.getUnitById(id);
        assertEquals(royal.getDepartureLocation(),
                restored.getDepartureLocation());
        final PlaceHighSea voyage = (PlaceHighSea) restored.getPlace();
        assertEquals(voyage.getOriginalTurns(), voyage.getRemainigTurns());
        assertEquals(
                AtlanticVoyageDuration.calculate(voyage.getWeather(), false,
                        restored.getDepartureDirection().orElseThrow(),
                        restored.getType().getSpeed()),
                voyage.getOriginalTurns());
    }

    @Test
    void royalCarrierWithoutArrivalLaneDoesNotCreateAUnit() {
        prepare(false, true);
        data.setModel(model.save());
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.entrySet()
                .removeIf(entry -> entry.getValue() == TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final int before = model.getAllUnits().size();
        assertThrows(IllegalStateException.class, () -> model
                .createCargoShipForKing(model.getPlayerByName("Dutch's King")));
        assertEquals(before, model.getAllUnits().size());
    }
}
