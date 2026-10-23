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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * R-0105: eligibility by exact national support, date and coast, and the once
 * only muster of armed veterans per colony. Colony 0 of the thrive scenario has
 * five residents, so 600, 900 and 1000 liberty are 60, 90 and 100 percent.
 */
class IndependenceDeclarationTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Colony 0 and colony 1 of the scenario. */
    private static final Location PORT = Location.of(25, 14);
    private static final Location OTHER = Location.of(22, 12);

    private static final Location OPEN_GROUND = Location.of(28, 10);

    private static final int FIRST_ID = 9001;

    private GameModelPo data(final boolean coastal) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        data.getModel().getCalendar().setEndYear(1900);
        shapeCoast(data, coastal);
        return data;
    }

    /**
     * Land around every colony, then one water square beside colony 0 when the
     * nation should own a coastal colony.
     */
    private void shapeCoast(final GameModelPo data, final boolean coastal) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (final ColonyPo colony : data.getModel().getColonies()) {
            for (final Location neighbor : colony.getLocation()
                    .getNeighbors()) {
                terrain.put(neighbor, TerrainType.GRASSLAND);
            }
        }
        terrain.put(OPEN_GROUND, TerrainType.GRASSLAND);
        final Location water = PORT.add(Direction.north.getVector());
        if (coastal) {
            terrain.put(water, TerrainType.OCEAN);
        }
        data.getModel().getMap().setTerrainType(terrain);
        final java.util.Set<Location> trees = data.getModel().getMap()
                .getTreeSet();
        trees.remove(water);
        data.getModel().getMap().setTrees(trees);
        final java.util.Set<Location> fields = data.getModel().getMap()
                .getFieldSet();
        fields.remove(water);
        data.getModel().getMap().setFields(fields);
        data.getModel().getColonies().stream()
                .flatMap(colony -> colony.getColonyFields().stream())
                .filter(field -> field.getWorkerId() != null)
                .forEach(field -> field.setProducedGoodsType(GoodsType.CORN));
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private ColonyPo savedColony(final GameModelPo data, final int id) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private int residents(final GameModelPo data, final int colonyId) {
        return load(data).getColonyById(colonyId).orElseThrow().getPopulation();
    }

    private long allResidents(final GameModelPo data) {
        return load(data).getColonies().stream()
                .mapToLong(Colony::getPopulation).sum();
    }

    private void libertyPerResident(final GameModelPo data, final int colonyId,
            final long perResident) {
        savedColony(data, colonyId)
                .setLiberty(perResident * residents(data, colonyId));
    }

    private void fullSupport(final GameModelPo data) {
        for (final ColonyPo colony : data.getModel().getColonies()) {
            libertyPerResident(data, colony.getId(), 200L);
        }
    }

    /** Spread an exact national numerator over the colonies up to capacity. */
    private void nationalNumerator(final GameModelPo data,
            final long numerator) {
        final Model probe = load(data);
        long remaining = numerator;
        for (final Colony colony : probe.getColonies()) {
            final long put = Math.min(200L * colony.getPopulation(), remaining);
            savedColony(data, colony.getId()).setLiberty(put);
            remaining -= put;
        }
        assertEquals(0L, remaining);
    }

    private void date(final GameModelPo data, final int startYear,
            final int turns) {
        data.getModel().getCalendar().setStartYear(startYear);
        data.getModel().getCalendar().setNumberOfPlayedTurns(turns);
    }

    private UnitPo soldier(final int id, final UnitType type, final Location at,
            final boolean armed, final boolean mounted, final boolean veteran) {
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        final UnitPo out = new UnitPo();
        out.setId(id);
        out.setType(type);
        out.setOwnerId("Dutch");
        out.setPlaceMap(place);
        out.setAction(new UnitActionNoActionPo());
        out.setAvailableMoves(1);
        out.setHoldingGuns(armed);
        out.setMounted(mounted);
        out.setVeteran(veteran);
        out.setWorkUpdates(0);
        out.setMissionary(false);
        return out;
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private PlayerPo savedPlayer(final GameModelPo data, final String name) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> name.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private UnitFreeColonist colonist(final Model model, final int id) {
        return (UnitFreeColonist) model.getUnitById(id);
    }

    private static final class Events {
        private final AtomicInteger declared = new AtomicInteger();

        private Events(final Model model) {
            model.addListener(new ModelListenerAdapter() {
                @Override
                public void onIndependenceWasDeclared(
                        final IndependenceWasDeclaredEvent event) {
                    declared.incrementAndGet();
                }
            });
        }
    }

    @Test
    void exactNationalSupportDecidesNotItsRoundedDisplay() {
        final GameModelPo below = data(true);
        nationalNumerator(below, 100L * allResidents(below) - 1);
        final Model refused = load(below);
        final Player dutch = refused.getPlayerByName("Dutch");
        assertEquals(49, dutch.getNationalRebelSupport().getDisplayPercent());
        assertEquals(Optional.of(IndependenceProblem.INSUFFICIENT_SUPPORT),
                dutch.getIndependenceProblem());
        assertFalse(dutch.isPossibleToDecalareIndependence());
        final String before = snapshot(refused);
        assertThrows(IllegalStateException.class, dutch::declareIndependence);
        assertEquals(before, snapshot(refused));

        final GameModelPo exact = data(true);
        nationalNumerator(exact, 100L * allResidents(exact));
        final Model accepted = load(exact);
        final Player allowed = accepted.getPlayerByName("Dutch");
        assertEquals(50, allowed.getNationalRebelSupport().getDisplayPercent());
        assertTrue(allowed.getNationalRebelSupport().isAtLeastPercent(50));
        assertEquals(Optional.empty(), allowed.getIndependenceProblem());
        allowed.declareIndependence();
        assertTrue(allowed.isDeclaredIndependence());
    }

    @Test
    void nationalSupportIsWeightedByResidentsNotAveragedByColony() {
        // Colony 0 (five residents) has none and the colonies of one and six
        // residents have 78 percent: the unweighted mean of the colonies is
        // 52 percent, the weighted support is 156 * 7 / (200 * 12) = 45.5.
        final GameModelPo data = data(true);
        assertEquals(5, residents(data, 0));
        assertEquals(1, residents(data, 1));
        assertEquals(6, residents(data, 2));
        libertyPerResident(data, 1, 156L);
        libertyPerResident(data, 2, 156L);
        final Player weighted = load(data).getPlayerByName("Dutch");
        assertEquals(45,
                weighted.getNationalRebelSupport().getDisplayPercent());
        assertEquals(Optional.of(IndependenceProblem.INSUFFICIENT_SUPPORT),
                weighted.getIndependenceProblem());
    }

    @ParameterizedTest
    @CsvSource({ "1490,0,true", "1600,0,true", "1700,0,true", "1700,399,true",
            "1700,400,false", "1700,404,false" })
    void declarationIsOpenOnlyBeforeSpring1800(final int startYear,
            final int turns, final boolean open) {
        // 1700 plus 399 turns is winter 1799; 400 turns is spring 1800.
        final GameModelPo data = data(true);
        fullSupport(data);
        date(data, startYear, turns);
        final Player dutch = load(data).getPlayerByName("Dutch");
        assertEquals(open, dutch.isPossibleToDecalareIndependence());
        assertEquals(
                open ? Optional.empty()
                        : Optional.of(IndependenceProblem.TOO_LATE),
                dutch.getIndependenceProblem());
    }

    @Test
    void oneOwnedCoastalColonyIsEnoughAndNoCoastRefuses() {
        final GameModelPo landlocked = data(false);
        fullSupport(landlocked);
        final Model refused = load(landlocked);
        final Player dutch = refused.getPlayerByName("Dutch");
        assertEquals(Optional.of(IndependenceProblem.NO_COASTAL_COLONY),
                dutch.getIndependenceProblem());
        final String before = snapshot(refused);
        assertThrows(IllegalStateException.class, dutch::declareIndependence);
        assertEquals(before, snapshot(refused));

        final GameModelPo coastal = data(true);
        fullSupport(coastal);
        final Model accepted = load(coastal);
        assertTrue(accepted
                .isOwnedCoastalPort(accepted.getPlayerByName("Dutch"), PORT));
        assertFalse(accepted
                .isOwnedCoastalPort(accepted.getPlayerByName("Dutch"), OTHER));
        assertEquals(Optional.empty(),
                accepted.getPlayerByName("Dutch").getIndependenceProblem());
    }

    @Test
    void onlyTheActiveColonialPlayerMayDeclare() {
        final GameModelPo data = data(true);
        fullSupport(data);
        final Model model = load(data);
        final Player king = model.getPlayerByName("Dutch's King");
        assertEquals(Optional.of(IndependenceProblem.UNAVAILABLE),
                king.getIndependenceProblem());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, king::declareIndependence);
        assertEquals(before, snapshot(model));

        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        final Model kingsTurn = load(data);
        final Player dutch = kingsTurn.getPlayerByName("Dutch");
        final String kingsBefore = snapshot(kingsTurn);
        assertThrows(IllegalStateException.class, dutch::declareIndependence);
        assertEquals(kingsBefore, snapshot(kingsTurn));
        assertFalse(dutch.isDeclaredIndependence());
    }

    /**
     * Five armed veterans on colony 0 (a foot soldier, a dragoon, a blacksmith
     * expert, a foot soldier and a dragoon), two on colony 1, plus units that
     * must never be mustered.
     */
    private GameModelPo musterData(final long colonyZeroPerResident) {
        final GameModelPo data = data(true);
        fullSupport(data);
        libertyPerResident(data, 0, colonyZeroPerResident);
        final List<UnitPo> units = data.getModel().getUnits();
        units.add(soldier(9001, UnitType.COLONIST, PORT, true, false, true));
        units.add(soldier(9002, UnitType.COLONIST, PORT, true, true, true));
        units.add(soldier(9003, UnitType.MASTER_BLACKSMITH, PORT, true, false,
                true));
        units.add(soldier(9004, UnitType.COLONIST, PORT, true, false, true));
        units.add(soldier(9005, UnitType.COLONIST, PORT, true, true, true));
        // An unarmed veteran and an armed non-veteran are never mustered.
        units.add(soldier(9006, UnitType.COLONIST, PORT, false, false, true));
        units.add(soldier(9007, UnitType.COLONIST, PORT, true, false, false));
        units.add(soldier(9008, UnitType.COLONIST, OTHER, true, false, true));
        units.add(soldier(9009, UnitType.COLONIST, OTHER, true, true, true));
        // A veteran passenger in the ship hold and a veteran field worker.
        units.stream().filter(unit -> unit.getId() == 13).findFirst()
                .orElseThrow().setVeteran(true);
        units.stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setVeteran(true);
        return data;
    }

    private List<Integer> expectedMustered(final int colonyZeroCount) {
        final List<Integer> expected = new ArrayList<>();
        for (int index = 0; index < colonyZeroCount; index++) {
            expected.add(FIRST_ID + index);
        }
        expected.add(9008);
        expected.add(9009);
        return expected;
    }

    private List<Integer> roleHolders(final Model model) {
        final List<Integer> out = new ArrayList<>();
        model.getAllUnits().stream().filter(UnitFreeColonist.class::isInstance)
                .map(UnitFreeColonist.class::cast)
                .filter(unit -> unit.getMilitaryRole().isSpecial())
                .forEach(unit -> out.add(unit.getId()));
        out.sort(Integer::compare);
        return out;
    }

    @ParameterizedTest
    @CsvSource({ "120,3", "180,4", "200,5", "99,0" })
    void eachSupportingColonyMustersTheExactFractionByStableId(
            final long perResident, final int expectedAtColonyZero) {
        // V=5 gives 3, 4 and 5 at 60, 90 and 100 percent and none at 49.5.
        final GameModelPo data = musterData(perResident);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(Optional.empty(), dutch.getIndependenceProblem());
        final List<Integer> expected = expectedMustered(expectedAtColonyZero);
        final List<Integer> preview = new ArrayList<>();
        dutch.getIndependenceMusterPreview()
                .forEach(unit -> preview.add(unit.getId()));
        assertEquals(expected, preview);
        assertEquals(List.of(), roleHolders(model));

        final int unitCount = model.getAllUnits().size();
        final int gold = dutch.getGold();
        final int muskets = model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.MUSKET).getAmount();
        final List<UnitFreeColonist> before = new ArrayList<>();
        final List<String> shape = new ArrayList<>();
        for (final int id : expected) {
            before.add(colonist(model, id));
            shape.add(describe(colonist(model, id)));
        }
        final Events events = new Events(model);

        dutch.declareIndependence();

        assertEquals(1, events.declared.get());
        assertTrue(dutch.isDeclaredIndependence());
        assertEquals(expected, roleHolders(model));
        for (int index = 0; index < expected.size(); index++) {
            final UnitFreeColonist after = colonist(model, expected.get(index));
            assertSame(before.get(index), after);
            assertEquals(MilitaryRole.CONTINENTAL, after.getMilitaryRole());
            assertEquals(shape.get(index), describe(after));
        }
        // Nothing is created, paid or destroyed by the muster.
        assertEquals(unitCount, model.getAllUnits().size());
        assertEquals(gold, dutch.getGold());
        assertEquals(muskets, model.getColonyById(0).orElseThrow()
                .getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        // Excluded units keep the ordinary role.
        for (final int id : List.of(9006, 9007, 13, 0)) {
            assertEquals(MilitaryRole.COLONIAL,
                    colonist(model, id).getMilitaryRole());
        }
        // The third unit by ID is the blacksmith expert.
        assertEquals(
                expectedAtColonyZero >= 3 ? MilitaryRole.CONTINENTAL
                        : MilitaryRole.COLONIAL,
                colonist(model, 9003).getMilitaryRole());
    }

    private String describe(final UnitFreeColonist unit) {
        return unit.getType() + ":" + unit.isHoldingGuns() + ":"
                + unit.isMounted() + ":" + unit.isVeteran() + ":"
                + unit.getLocation() + ":" + unit.getOwner().getName();
    }

    @Test
    void civilianExpertiseAndEquipmentSurviveTheMuster() {
        final GameModelPo data = musterData(200L);
        final Model model = load(data);
        final UnitFreeColonist expert = colonist(model, 9003);
        assertEquals(UnitType.MASTER_BLACKSMITH, expert.getType());
        model.getPlayerByName("Dutch").declareIndependence();
        assertSame(expert, model.getUnitById(9003));
        assertEquals(MilitaryRole.CONTINENTAL, expert.getMilitaryRole());
        assertEquals(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertTrue(expert.isHoldingGuns());
        assertTrue(colonist(model, 9002).isMounted());
        assertTrue(colonist(model, 9002).isHoldingGuns());
    }

    @Test
    void declarationAndMusterSurviveSaveAndContinueWithoutRepeating() {
        final GameModelPo data = musterData(120L);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        dutch.declareIndependence();
        final List<Integer> mustered = roleHolders(model);
        assertEquals(expectedMustered(3), mustered);

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Events events = new Events(restored);
        assertEquals(snapshot(model), snapshot(restored));
        assertTrue(restored.getPlayerByName("Dutch").isDeclaredIndependence());
        assertEquals(mustered, roleHolders(restored));
        assertEquals(Optional.of(IndependenceProblem.ALREADY_DECLARED),
                restored.getPlayerByName("Dutch").getIndependenceProblem());
        assertEquals(List.of(), restored.getPlayerByName("Dutch")
                .getIndependenceMusterPreview());
        final String before = snapshot(restored);
        assertThrows(IllegalStateException.class,
                restored.getPlayerByName("Dutch")::declareIndependence);
        assertEquals(before, snapshot(restored));
        assertEquals(0, events.declared.get());
        assertEquals(mustered, roleHolders(restored));
    }

    @Test
    void aVetoLeavesTheStateUnchangedAndAnnouncesNothing() {
        final GameModelPo data = musterData(200L);
        final Model model = load(data);
        final Events events = new Events(model);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onBeforeDeclaringIndependence(
                    final BeforeDeclaringIndependenceEvent event) {
                event.stopEventExecution();
            }
        });
        final String before = snapshot(model);
        model.getPlayerByName("Dutch").declareIndependence();
        assertFalse(model.getPlayerByName("Dutch").isDeclaredIndependence());
        assertEquals(List.of(), roleHolders(model));
        assertEquals(before, snapshot(model));
        assertEquals(0, events.declared.get());
    }

    @Test
    void aReentrantDeclarationCannotPartiallyConvert() {
        final GameModelPo data = musterData(200L);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Events events = new Events(model);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onBeforeDeclaringIndependence(
                    final BeforeDeclaringIndependenceEvent event) {
                dutch.declareIndependence();
            }
        });
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, dutch::declareIndependence);
        assertFalse(dutch.isDeclaredIndependence());
        assertEquals(List.of(), roleHolders(model));
        assertEquals(before, snapshot(model));
        assertEquals(0, events.declared.get());
    }

    @Test
    void aStaleConfirmationIsRevalidatedAfterTheVetoCallbacks() {
        // Winter 1799 allows the declaration; a callback advances the calendar
        // to spring 1800 before the commit.
        final GameModelPo data = musterData(200L);
        date(data, 1700, 399);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        assertTrue(dutch.isPossibleToDecalareIndependence());
        final Events events = new Events(model);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onBeforeDeclaringIndependence(
                    final BeforeDeclaringIndependenceEvent event) {
                model.getCalendar().endRound();
            }
        });
        assertThrows(IllegalStateException.class, dutch::declareIndependence);
        assertFalse(dutch.isDeclaredIndependence());
        assertEquals(List.of(), roleHolders(model));
        assertEquals(0, events.declared.get());
        assertEquals(Optional.of(IndependenceProblem.TOO_LATE),
                dutch.getIndependenceProblem());
    }

    @Test
    void aPauseIntroducedByACallbackIsRevalidatedBeforeAnyChange() {
        final GameModelPo data = musterData(200L);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Events events = new Events(model);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onBeforeDeclaringIndependence(
                    final BeforeDeclaringIndependenceEvent event) {
                awaitImmigrationChoice(model);
            }
        });
        assertThrows(TurnPreparationException.class,
                dutch::declareIndependence);
        assertFalse(dutch.isDeclaredIndependence());
        assertEquals(List.of(), roleHolders(model));
        assertEquals(0, events.declared.get());
    }

    /** Simulate a pending choice that a callback could not legally create. */
    private static void awaitImmigrationChoice(final Model model) {
        try {
            final java.lang.reflect.Field manager = Model.class
                    .getDeclaredField("gameManager");
            manager.setAccessible(true);
            final java.lang.reflect.Field flag = GameManager.class
                    .getDeclaredField("awaitingImmigrationChoice");
            flag.setAccessible(true);
            flag.setBoolean(manager.get(model), true);
        } catch (final ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    /**
     * A declared nation whose first colony can take the 50 muskets back: the
     * scenario's warehouse holds 100, which is exactly the basic limit.
     */
    private Model declaredWithRoomForMuskets(final boolean room) {
        final GameModelPo data = musterData(200L);
        if (room) {
            savedColony(data, 0).getColonyWarehouse().put("MUSKET", 0);
        }
        final Model model = load(data);
        model.getPlayerByName("Dutch").declareIndependence();
        return model;
    }

    private void placeInJob(final Model model, final UnitFreeColonist unit,
            final String job) {
        final Colony colony = model.getColonyById(0).orElseThrow();
        if ("field".equals(job)) {
            unit.placeToColonyField(
                    colony.getColonyFieldInDirection(Direction.east),
                    GoodsType.CORN);
        } else {
            unit.placeToColonyStructureSlot(
                    colony.getConstructionByType(ConstructionType.TOWN_HALL)
                            .getFirstEmptySlot().orElseThrow());
        }
    }

    @ParameterizedTest
    @CsvSource({ "field", "building" })
    void aColonyJobListenerNeverSeesASpecialRoleInsideTheJob(final String job) {
        final Model model = declaredWithRoomForMuskets(true);
        final UnitFreeColonist continental = colonist(model, 9001);
        assertEquals(MilitaryRole.CONTINENTAL, continental.getMilitaryRole());
        final List<String> observed = new ArrayList<>();
        final Runnable observe = () -> {
            // The unit as a save stores it now, and the whole saved game as
            // the loader sees it now.
            final Model loaded = Model.make(model.save(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            observed.add((continental.isAtPlaceColonyField()
                    || continental.isAtPlaceConstructionSlot()) + ":"
                    + continental.save().getMilitaryRole() + ":"
                    + colonist(loaded, 9001).getMilitaryRole());
        };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMovedToColonyField(
                    final UnitMovedToColonyFieldEvent event) {
                observe.run();
            }

            @Override
            public void onUnitMovedToConstruction(
                    final UnitMovedToConstructionEvent event) {
                observe.run();
            }
        });
        placeInJob(model, continental, job);
        assertEquals(List.of("true:COLONIAL:COLONIAL"), observed);
        assertEquals(MilitaryRole.COLONIAL, continental.getMilitaryRole());
        assertFalse(continental.isHoldingGuns());
        assertEquals(50, model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.MUSKET).getAmount());
    }

    @ParameterizedTest
    @CsvSource({ "field", "building" })
    void aRejectedJobPlacementKeepsTheRoleEquipmentAndPlace(final String job) {
        // The full warehouse cannot take the muskets back, so the real
        // validation refuses before anything changes.
        final Model model = declaredWithRoomForMuskets(false);
        final UnitFreeColonist continental = colonist(model, 9001);
        final String before = snapshot(model);
        final AtomicInteger announced = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMovedToColonyField(
                    final UnitMovedToColonyFieldEvent event) {
                announced.incrementAndGet();
            }

            @Override
            public void onUnitMovedToConstruction(
                    final UnitMovedToConstructionEvent event) {
                announced.incrementAndGet();
            }
        });
        assertThrows(IllegalStateException.class,
                () -> placeInJob(model, continental, job));
        assertEquals(before, snapshot(model));
        assertEquals(0, announced.get());
        assertEquals(MilitaryRole.CONTINENTAL, continental.getMilitaryRole());
        assertTrue(continental.isHoldingGuns());
        assertTrue(continental.isAtPlaceLocation());
    }

    @Test
    void onlyOneOwnedCoastalColonyBeforeTheLegacyYearIsEnough() {
        // Colony 0 alone touches water; no legacy 1700 or three-colony gate
        // applies in 1490.
        final GameModelPo data = data(true);
        fullSupport(data);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(1490, model.getCalendar().getCurrentYear());
        assertEquals(3, dutch.getColonies().size());
        assertEquals(1L, dutch.getColonies().stream().filter(
                colony -> model.isOwnedCoastalPort(dutch, colony.getLocation()))
                .count());
        assertEquals(Optional.empty(), dutch.getIndependenceProblem());
        dutch.declareIndependence();
        assertTrue(dutch.isDeclaredIndependence());
    }

    @Test
    void afterListenersSeeTheCompleteDeclaredState() {
        final GameModelPo data = musterData(200L);
        final Model model = load(data);
        final List<String> seen = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onIndependenceWasDeclared(
                    final IndependenceWasDeclaredEvent event) {
                seen.add(event.getWhoDecalareIt().isDeclaredIndependence() + ":"
                        + roleHolders(model));
            }
        });
        model.getPlayerByName("Dutch").declareIndependence();
        assertEquals(List.of("true:" + expectedMustered(5)), seen);
    }

    @Test
    void savedRolesNeedAMatchingOwnerAndPlacement() {
        final GameModelPo data = data(true);
        fullSupport(data);
        final UnitPo soldier = soldier(9001, UnitType.COLONIST, PORT, true,
                false, true);
        data.getModel().getUnits().add(soldier);

        // Continental service needs a nation that declared independence.
        soldier.setMilitaryRole(MilitaryRole.CONTINENTAL);
        assertThrows(RuntimeException.class, () -> load(data));
        RebellionFixtures.declare(data.getModel(), savedPlayer(data, "Dutch"));
        final Model model = load(data);
        assertEquals(MilitaryRole.CONTINENTAL,
                colonist(model, 9001).getMilitaryRole());
        assertEquals(snapshot(model), snapshot(load(wrap(model))));

        // A royal role belongs to a Crown, a missing role is never guessed.
        soldier.setMilitaryRole(MilitaryRole.ROYAL);
        assertThrows(RuntimeException.class, () -> load(data));
        soldier.setMilitaryRole(null);
        assertThrows(RuntimeException.class, () -> load(data));

        // Only a colonist outside a colony job may hold a role.
        soldier.setMilitaryRole(MilitaryRole.COLONIAL);
        final UnitPo worker = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        worker.setMilitaryRole(MilitaryRole.CONTINENTAL);
        assertThrows(RuntimeException.class, () -> load(data));
        worker.setMilitaryRole(MilitaryRole.COLONIAL);
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        ship.setMilitaryRole(MilitaryRole.CONTINENTAL);
        assertThrows(RuntimeException.class, () -> load(data));
        ship.setMilitaryRole(MilitaryRole.COLONIAL);
        assertEquals(MilitaryRole.COLONIAL,
                colonist(load(data), 9001).getMilitaryRole());
    }

    private GameModelPo wrap(final Model model) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.setModel(model.save());
        return data;
    }

    @Test
    void aCrownHoldsRoyalRolesThroughSaveAndContinue() {
        final GameModelPo data = data(true);
        fullSupport(data);
        final UnitPo royal = soldier(9001, UnitType.COLONIST, OPEN_GROUND, true,
                false, false);
        royal.setOwnerId("Dutch's King");
        royal.setMilitaryRole(MilitaryRole.ROYAL);
        data.getModel().getUnits().add(royal);
        final Model model = load(data);
        assertEquals(MilitaryRole.ROYAL,
                colonist(model, 9001).getMilitaryRole());
        final Model restored = load(wrap(model));
        assertEquals(MilitaryRole.ROYAL,
                colonist(restored, 9001).getMilitaryRole());
        assertEquals(snapshot(model), snapshot(restored));
    }

    @Test
    void ordinaryVeteransAreNeverGivenARoleByInference() {
        final GameModelPo data = data(true);
        fullSupport(data);
        data.getModel().getUnits()
                .add(soldier(9001, UnitType.COLONIST, PORT, true, false, true));
        final Model model = load(data);
        assertEquals(MilitaryRole.COLONIAL,
                colonist(model, 9001).getMilitaryRole());
        assertTrue(colonist(model, 9001).isVeteran());
        assertEquals(3.0, colonist(model, 9001).getMilitaryStrenght(), 1e-9);
    }
}
