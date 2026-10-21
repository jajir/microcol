package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Colony;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.RivalExpansionObjective;
import org.microcol.model.RivalExpansionObjective.Stage;
import org.microcol.model.RivalTradeFixtures;
import org.microcol.model.RivalTradeObjective;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.EducationCoursePo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.Ship;

import com.google.common.eventbus.EventBus;

/**
 * Rival second-colony expansion decided by the real robot turn. Each scenario
 * builds its own colony with an explicit crew, so the reserve, the workforce
 * exclusions and the transport choice are decided on known state. A decision is
 * always made by one owner turn of the robot, never by a shortcut.
 */
class RivalExpansionCycleTest {
    private static final String RIVAL = "France";
    private static final int FOUNDER_ID = 4;
    private static final int SHIP_ID = 5;
    private static final int SECOND_SHIP_ID = 6;
    /** Crew colonists take IDs from here, one for each setup entry. */
    private static final int CREW_FIRST_ID = 10;
    private static final int COLONY_ID = 0;
    private static final Location PORT = Location.of(10, 10);
    private static final Location COLONY = Location.of(11, 10);
    /** Land beside the colony that the crew works as corn fields. */
    private static final List<Location> CORN_FIELDS = List.of(
            Location.of(11, 9), Location.of(12, 9), Location.of(12, 10),
            Location.of(12, 11));
    /** Land beside the colony with trees, so colonists can cut lumber. */
    private static final List<Location> FORESTS = List.of(Location.of(10, 9),
            Location.of(10, 11), Location.of(11, 11));
    /** Land strip joining the colony to the overland site block. */
    private static final List<Location> STRIP = List.of(Location.of(13, 10),
            Location.of(14, 10), Location.of(15, 10));
    /** Coastal grassland 3x3 blocks, reachable by sea or by the strip. */
    private static final Location SEA_BLOCK = Location.of(20, 10);
    private static final Location OVERLAND_BLOCK = Location.of(16, 10);
    private static final int FIVE_TURNS = 5;
    /** Finite bound for any expansion, in owner turns of the rival. */
    private static final int MAX_TURNS = 20;
    private static final int DEFAULT_FOOD = 100;

    private Model model;

    private SimpleAiPlayer ai;

    @AfterEach
    void stopRobot() {
        if (ai != null) {
            ai.stop();
        }
    }

    /** Where the land and sea of a scenario lie. */
    private enum Layout {
        /** Only the colony land; a site exists only by sea. */
        SEA,
        /** The strip and a site block that the founder can walk to. */
        OVERLAND,
        /** The colony land alone, so no legal site exists. */
        NONE
    }

    /** The colony and its crew before the first owner turn. */
    private static final class Setup {
        private Layout layout = Layout.SEA;
        private boolean knowsSite = true;
        private boolean stockade;
        private int food = DEFAULT_FOOD;
        /** Job of each crew colonist, in crew order. */
        private final List<GoodsType> jobs = new ArrayList<>();
        /** Crew indices that hold guns. */
        private final Set<Integer> armed = new HashSet<>();
        /** Crew indices that are students of a course. */
        private final Set<Integer> students = new HashSet<>();

        private Setup crew(final GoodsType... goods) {
            jobs.addAll(Arrays.asList(goods));
            return this;
        }

        private Setup layout(final Layout value) {
            layout = value;
            return this;
        }

        private Setup unknownSite() {
            knowsSite = false;
            return this;
        }

        private Setup stockade() {
            stockade = true;
            return this;
        }

        private Setup food(final int amount) {
            food = amount;
            return this;
        }

        private Setup armed(final int crewIndex) {
            armed.add(crewIndex);
            return this;
        }

        private Setup student(final int crewIndex) {
            students.add(crewIndex);
            return this;
        }
    }

    /**
     * Open ocean with the colony land, the requested site and the French ships
     * in the port. The French know the whole map, unless the scenario hides the
     * site.
     */
    private Model world(final Setup s) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(RIVAL);
        final Map<Location, TerrainType> terrain = new HashMap<>();
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                final Location at = Location.of(x, y);
                terrain.put(at, TerrainType.OCEAN);
                if (s.knowsSite || Math.abs(x - COLONY.getX()) <= 4
                        && Math.abs(y - COLONY.getY()) <= 4) {
                    known.add(at);
                }
            }
        }
        terrain.put(COLONY, TerrainType.GRASSLAND);
        CORN_FIELDS.forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        FORESTS.forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        if (s.layout == Layout.SEA) {
            coastalBlock(terrain, SEA_BLOCK);
        }
        if (s.layout == Layout.OVERLAND) {
            STRIP.forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
            coastalBlock(terrain, OVERLAND_BLOCK);
        }
        final WorldMapPo map = data.getModel().getMap();
        map.setTerrainType(terrain);
        clearFeatures(map);
        map.setTrees(Set.copyOf(FORESTS));
        final PlayerPo rival = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals(RIVAL))
                .findFirst().orElseThrow();
        rival.getVisible().setVisibility(known, 50, 30);
        final List<UnitPo> units = new ArrayList<>(data.getModel().getUnits());
        final UnitPo ship = units.stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
        ship.getPlaceMap().setLocation(PORT);
        units.add(extraShip(ship, SECOND_SHIP_ID, PORT));
        for (int index = 0; index < s.jobs.size(); index++) {
            units.add(colonist(ship, CREW_FIRST_ID + index));
        }
        data.getModel().setUnits(units);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** A 3x3 grassland block whose west edge is the given square. */
    private static void coastalBlock(final Map<Location, TerrainType> terrain,
            final Location west) {
        for (int x = west.getX(); x <= west.getX() + 2; x++) {
            for (int y = west.getY() - 1; y <= west.getY() + 1; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
    }

    /**
     * Removes every feature the synthetic land does not need, so ocean squares
     * hold no trees, rivers, resources or fields that the model would reject.
     */
    private static void clearFeatures(final WorldMapPo map) {
        map.setTrees(Set.of());
        map.setRivers(Map.of());
        map.setRiverSegments(List.of());
        map.setResources(Map.of());
        map.setMineralReserves(Map.of());
        map.setFields(Set.of());
        map.setRoads(Set.of());
        map.setCleared(Set.of());
    }

    /**
     * Founds the rival colony, moves both ships into the port, then fills the
     * crew by real jobs. A stockade or a course is saved and reloaded in the
     * same way as a Continue, because the model checks them only on load.
     */
    private void prepare(final Setup s) {
        if (ai != null) {
            // A test that prepares twice must not leave a live robot behind.
            ai.stop();
        }
        model = world(s);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        founder.placeToLocation(COLONY);
        founder.startTurn();
        model.buildColony(france(), founder, "Rouen");
        for (final int id : List.of(SHIP_ID, SECOND_SHIP_ID)) {
            final Ship carrier = (Ship) model.getUnitById(id);
            carrier.startTurn();
            if (!carrier.getLocation().equals(COLONY)) {
                model.moveUnit(carrier, Path.of(List.of(COLONY)));
            }
        }
        if (s.stockade) {
            reloadWith(saved -> {
                final ConstructionPo stockade = new ConstructionPo();
                stockade.setType(ConstructionType.STOCKADE);
                saved.getConstructions().add(stockade);
            });
        }
        for (int index = 0; index < s.jobs.size(); index++) {
            employ(model.getUnitById(CREW_FIRST_ID + index), s.jobs.get(index));
        }
        if (!s.students.isEmpty() || !s.armed.isEmpty()) {
            // Taking a job unequips a colonist, so guns and courses are saved
            // after the jobs: the reload keeps a resident that holds guns.
            final ModelPo saved = model.save();
            saved.getColonies().get(0)
                    .setEducationCourses(s.students.stream()
                            .map(index -> course(CREW_FIRST_ID + index))
                            .collect(Collectors.toList()));
            for (final UnitPo unit : saved.getUnits()) {
                if (s.armed.stream().anyMatch(
                        index -> CREW_FIRST_ID + index == unit.getId())) {
                    unit.setHoldingGuns(true);
                }
            }
            model = Model.make(saved,
                    MissionImpl.getPredefinedGameOverEvaluators());
        }
        setCorn(s.food);
        ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
    }

    /** Save, change the colony record, and load the game again. */
    private void reloadWith(final Consumer<ColonyPo> change) {
        final ModelPo saved = model.save();
        change.accept(saved.getColonies().get(0));
        model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Puts a crewmember to work the way the robot's workforce does. */
    private void employ(final Unit worker, final GoodsType goods) {
        final ColonyJobChoice choice = colony().getJobChoices(worker).stream()
                .filter(option -> option.getUnavailableReason().isEmpty()
                        && option.getGoodsType() == goods
                        && option.getPotential().isPresent())
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "No free " + goods + " job for " + worker));
        choice.execute();
    }

    /** Save and load: the game and the robot continue from the saved state. */
    private void continueGame() {
        ai.stop();
        model = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
    }

    /** One owner turn of the robot, without moving any unit itself. */
    private void turnStarted() {
        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> ai.turnStarted());
    }

    /**
     * One France owner turn: every own unit regains its movement, then the
     * robot plays its turn.
     */
    private void rivalTurn() {
        for (final Unit unit : List.copyOf(model.getAllUnits())) {
            if (unit.getOwner() == france()) {
                unit.startTurn();
            }
        }
        turnStarted();
    }

    private Player france() {
        return model.getPlayerByName(RIVAL);
    }

    private Colony colony() {
        return model.getColonyById(COLONY_ID).orElseThrow();
    }

    private Colony secondColony() {
        return model.getColonies(france()).stream()
                .filter(colony -> colony.getId() != COLONY_ID).findFirst()
                .orElseThrow();
    }

    private int colonyCount() {
        return model.getColonies(france()).size();
    }

    private Set<Integer> residentIds() {
        return colony().getUnitsInsideColony().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Sets the corn stock through the public, validated warehouse operations.
     */
    private void setCorn(final int amount) {
        colony().getWarehouse()
                .removeGoods(colony().getWarehouse().getGoods(GoodsType.CORN));
        colony().getWarehouse().addGoods(Goods.of(GoodsType.CORN, amount));
    }

    private int corn() {
        return colony().getWarehouse().getGoods(GoodsType.CORN).getAmount();
    }

    /** No expansion was released: the four residents stay, founder included. */
    private void assertNoRelease(final int residents) {
        assertTrue(france().getRivalExpansionObjective().isEmpty(),
                "No expansion may start from this state");
        assertEquals(residents, colony().getUnitsInsideColony().size());
        assertTrue(colony().getUnitsInsideColony()
                .contains(model.getUnitById(FOUNDER_ID)));
        assertEquals(1, colonyCount());
    }

    /** The founder left the colony square toward its saved site. */
    private RivalExpansionObjective assertReleased() {
        assertTrue(france().getRivalExpansionObjective().isPresent(),
                "The expansion must release a founder");
        return france().getRivalExpansionObjective().orElseThrow();
    }

    private static UnitPo extraShip(final UnitPo template, final int id,
            final Location at) {
        final UnitPo extra = new UnitPo();
        extra.setId(id);
        extra.setType(UnitType.GALLEON);
        extra.setOwnerId(RIVAL);
        extra.setAction(template.getAction());
        extra.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        extra.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int index = 0; index < template.getCargo().getSlots()
                .size(); index++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        extra.setCargo(cargo);
        return extra;
    }

    /** A French colonist standing on the colony square, not yet working. */
    private static UnitPo colonist(final UnitPo template, final int id) {
        final UnitPo crew = new UnitPo();
        crew.setId(id);
        crew.setType(UnitType.COLONIST);
        crew.setOwnerId(RIVAL);
        crew.setAction(template.getAction());
        crew.setWorkUpdates(0);
        crew.setMissionary(false);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.west);
        crew.setPlaceMap(place);
        crew.setAvailableMoves(1);
        return crew;
    }

    /** A student course that a colonist attends without a school building. */
    private static EducationCoursePo course(final int studentId) {
        final EducationCoursePo po = new EducationCoursePo();
        po.setStudentId(studentId);
        po.setSkill(UnitType.MASTER_BLACKSMITH);
        po.setCompletedUpdates(0);
        return po;
    }

    @Test
    void reserveReleasesOnlyWhenFoodCoversFiveTurnsOfRemainingResidents() {
        // Four residents; three remain after the founder leaves.
        final int needed = FIVE_TURNS * 3
                * UnitType.COLONIST.getAteFoodPerTurn();
        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN, GoodsType.CORN)
                .food(needed - 1));
        turnStarted();
        assertNoRelease(4);

        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN, GoodsType.CORN)
                .food(needed));
        turnStarted();
        assertEquals(FOUNDER_ID, assertReleased().getFounderId());
    }

    @Test
    void retainsTwoResidentsAndRefusesFewer() {
        prepare(new Setup().crew(GoodsType.CORN));
        turnStarted();
        assertNoRelease(2);

        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN));
        turnStarted();
        assertEquals(FOUNDER_ID, assertReleased().getFounderId());
    }

    @Test
    void fortifiedColonyKeepsItsHigherMinimum() {
        // A stockade needs more than three residents before one may leave.
        prepare(new Setup().stockade().crew(GoodsType.CORN, GoodsType.CORN));
        turnStarted();
        assertNoRelease(3);

        prepare(new Setup().stockade().crew(GoodsType.CORN, GoodsType.CORN,
                GoodsType.CORN));
        turnStarted();
        assertEquals(FOUNDER_ID, assertReleased().getFounderId());
    }

    @Test
    void negativeFoodBalanceAfterReleaseRefusesRelease() {
        // Founder grows food; three lumber colonists eat more than it grows.
        prepare(new Setup().crew(GoodsType.LUMBER, GoodsType.LUMBER,
                GoodsType.LUMBER));
        turnStarted();
        assertNoRelease(4);
    }

    @Test
    void armedAndTeachingColonistsAreNeverReleased() {
        // The armed and student colonists would be chosen first, since they
        // produce no food, if the exclusions were missing.
        prepare(new Setup().crew(GoodsType.LUMBER, GoodsType.LUMBER,
                GoodsType.CORN, GoodsType.CORN).armed(0).student(1));
        turnStarted();
        assertEquals(FOUNDER_ID, assertReleased().getFounderId());
        assertTrue(colony().getUnitsInsideColony()
                .contains(model.getUnitById(CREW_FIRST_ID)));
        assertTrue(colony().getUnitsInsideColony()
                .contains(model.getUnitById(CREW_FIRST_ID + 1)));
    }

    @Test
    void unknownOrMissingSiteMutatesNothing() {
        prepare(new Setup().unknownSite().crew(GoodsType.CORN, GoodsType.CORN,
                GoodsType.CORN));
        turnStarted();
        assertNoRelease(4);
        assertEquals(DEFAULT_FOOD, corn());
        assertEquals(COLONY, model.getUnitById(SECOND_SHIP_ID).getLocation());

        prepare(new Setup().layout(Layout.NONE).crew(GoodsType.CORN,
                GoodsType.CORN, GoodsType.CORN));
        turnStarted();
        assertNoRelease(4);
        assertEquals(DEFAULT_FOOD, corn());
    }

    @Test
    void seaJourneyFoundsTheSecondColonyOnceAndSurvivesReload() {
        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN,
                GoodsType.CORN));
        rivalTurn();
        final RivalExpansionObjective journey = assertReleased();
        assertEquals(Optional.of(SECOND_SHIP_ID), journey.getCarrierId());
        final Location site = journey.getSite();
        for (int turn = 0; turn < MAX_TURNS && colonyCount() < 2; turn++) {
            rivalTurn();
        }
        assertEquals(2, colonyCount(), "The second colony must be founded");
        assertEquals(site, secondColony().getLocation());
        assertEquals(List.of(model.getUnitById(FOUNDER_ID)),
                secondColony().getUnitsInsideColony());
        assertEquals(
                Set.of(CREW_FIRST_ID, CREW_FIRST_ID + 1, CREW_FIRST_ID + 2),
                residentIds());
        assertTrue(france().getRivalExpansionObjective().isEmpty());

        // Later turns and a reload found nothing more.
        rivalTurn();
        rivalTurn();
        assertEquals(2, colonyCount());
        continueGame();
        rivalTurn();
        assertEquals(2, colonyCount());
        assertTrue(france().getRivalExpansionObjective().isEmpty());
    }

    @Test
    void overlandSiteIsWalkedWithoutAShip() {
        prepare(new Setup().layout(Layout.OVERLAND).crew(GoodsType.CORN,
                GoodsType.CORN, GoodsType.CORN));
        rivalTurn();
        final RivalExpansionObjective journey = assertReleased();
        assertTrue(journey.getCarrierId().isEmpty(),
                "A land-connected site needs no transport");
        assertEquals(Stage.TRAVEL, journey.getStage());
        final Location site = journey.getSite();
        for (int turn = 0; turn < MAX_TURNS && colonyCount() < 2; turn++) {
            rivalTurn();
        }
        assertEquals(2, colonyCount(), "The walk must end in a founding");
        assertEquals(site, secondColony().getLocation());
        assertTrue(france().getRivalExpansionObjective().isEmpty());
    }

    @Test
    void transportWaitsInPortWhileReservesRefuseTheRelease() {
        // A known sea site and an idle second ship, but one resident only.
        prepare(new Setup());
        turnStarted();
        assertNoRelease(1);
        final Unit spare = model.getUnitById(SECOND_SHIP_ID);
        assertTrue(ai.isHeldCarrier(spare));
        assertFalse(ai.isHeldCarrier(model.getUnitById(SHIP_ID)),
                "The trade carrier is never held for the expansion");
        spare.startTurn();
        assertTrue(spare.getActionPoints() > 0);
        ai.moveUnit(spare);
        assertEquals(COLONY, spare.getLocation(),
                "The ordinary unit moves must not sail the transport away");

        // Without a known legal site nothing is held, so the ship may explore.
        prepare(new Setup().layout(Layout.NONE));
        turnStarted();
        assertFalse(ai.isHeldCarrier(model.getUnitById(SECOND_SHIP_ID)));
    }

    @Test
    void destroyedFounderRetiresTheExpansion() {
        // Two crew keep the colony alive after the founder is released.
        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN));
        final Unit founder = model.getUnitById(FOUNDER_ID);
        founder.placeToMap(COLONY);
        france().setRivalExpansionObjective(new RivalExpansionObjective(
                COLONY_ID, FOUNDER_ID, SHIP_ID, COLONY, Stage.TRAVEL));
        RivalTradeFixtures.destroyUnit(model, founder);
        turnStarted();
        assertTrue(france().getRivalExpansionObjective().isEmpty());
        assertEquals(1, colonyCount());
    }

    @Test
    void tradeCarrierIsNotGivenToTheExpansion() {
        // The crew and the known sea site let the expansion start, so only
        // the trade carrier keeps it from taking the first ship.
        prepare(new Setup().crew(GoodsType.CORN, GoodsType.CORN,
                GoodsType.CORN));
        france().setRivalTradeObjective(new RivalTradeObjective(COLONY_ID,
                SHIP_ID, RivalTradeObjective.Stage.TO_EUROPE));
        turnStarted();
        assertEquals(Optional.of(SECOND_SHIP_ID),
                assertReleased().getCarrierId());
        assertEquals(SHIP_ID,
                france().getRivalTradeObjective().orElseThrow().getCarrierId());
    }

    @Test
    void expansionCarrierIsNotGivenToTrade() {
        prepare(new Setup());
        france().setRivalExpansionObjective(new RivalExpansionObjective(
                COLONY_ID, FOUNDER_ID, SHIP_ID, COLONY, Stage.BOARDING));
        turnStarted();
        assertEquals(SECOND_SHIP_ID,
                france().getRivalTradeObjective().orElseThrow().getCarrierId());
    }

    @Test
    void midJourneyExpansionSurvivesSaveAndContinue() {
        prepare(new Setup());
        france().setRivalExpansionObjective(new RivalExpansionObjective(
                COLONY_ID, FOUNDER_ID, SHIP_ID, COLONY, Stage.TRAVEL));
        continueGame();
        final RivalExpansionObjective saved = france()
                .getRivalExpansionObjective().orElseThrow();
        assertEquals(COLONY_ID, saved.getColonyId());
        assertEquals(FOUNDER_ID, saved.getFounderId());
        assertEquals(Optional.of(SHIP_ID), saved.getCarrierId());
        assertEquals(COLONY, saved.getSite());
        assertEquals(Stage.TRAVEL, saved.getStage());
    }
}
