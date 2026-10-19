package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.GoodsType;
import org.microcol.model.TerrainType;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

/**
 * Real turn events drive the same rival and Crown players used in free play.
 */
class FirstColonyJourneyTest {
    private static final int FOUNDING_ROUND = 3;
    private static final Location COLONY_SITE = Location.of(21, 2);

    @TempDir
    Path directory;

    private GameModelPo expedition() {
        return new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String humanUnits(final Model model) {
        return new Gson().toJson(model.getPlayerByName("Dutch").getAllUnits()
                .stream().map(Unit::save).collect(Collectors.toList()));
    }

    private Colony reachColony(final Game game) throws Exception {
        if (game.model.getCalendar().getNumberOfPlayedTurns() == 0) {
            assertEquals(Location.of(29, 4),
                    game.model.getUnitById(5).getLocation());
            assertTrue(game.model.getUnitById(4).isAtCargoSlot());
        }
        while (game.model.getCalendar()
                .getNumberOfPlayedTurns() < FOUNDING_ROUND
                && game.model.getColonies().isEmpty()) {
            game.nextTurn();
            final int round = game.model.getCalendar().getNumberOfPlayedTurns();
            if (round == 1) {
                assertEquals(Location.of(23, 4),
                        game.model.getUnitById(5).getLocation());
                assertTrue(game.model.getUnitById(4).isAtCargoSlot());
                assertTrue(game.model.getColonies().isEmpty());
            } else if (round == 2) {
                assertEquals(Location.of(21, 3),
                        game.model.getUnitById(5).getLocation());
                assertTrue(game.model.getUnitById(4).isAtPlaceLocation());
                assertEquals(COLONY_SITE,
                        game.model.getUnitById(4).getLocation());
                assertEquals(0, game.model.getUnitById(4).getActionPoints());
                assertTrue(game.model.getColonies().isEmpty());
            }
        }
        assertEquals(FOUNDING_ROUND,
                game.model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(1, game.model.getColonies().size(),
                "The reference expedition must found its colony");
        assertEquals(COLONY_SITE,
                game.model.getColonies().get(0).getLocation());
        assertTrue(game.model.getMap().getTerrainTypeAt(COLONY_SITE).isLand());
        assertTrue(COLONY_SITE.getNeighbors().stream()
                .filter(game.model.getMap()::isValid).anyMatch(at -> game.model
                        .getMap().getTerrainTypeAt(at).isSea()));
        return game.model.getColonies().get(0);
    }

    @Test
    void realTurnsExploreLandAndFoundOneViableColonyWithTheOriginalSettler()
            throws Exception {
        try (Game game = new Game(load(expedition()))) {
            final String humanBefore = humanUnits(game.model);
            final Colony colony = reachColony(game);
            final Unit founder = game.model.getUnitById(4);
            assertSame(game.model.getPlayerByName("France"), colony.getOwner());
            assertEquals(List.of(founder), colony.getUnitsInsideColony());
            assertEquals(6, game.model.getAllUnits().size());
            assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getNetProduction() >= 0);
            assertEquals(humanBefore, humanUnits(game.model));
            final int id = colony.getId();
            final Location site = colony.getLocation();

            // Subsequent real turns feed the same resident and do not expand
            // again.
            game.nextTurn();
            game.nextTurn();
            game.nextTurn();
            assertEquals(1, game.model.getColonies().size());
            assertSame(colony, game.model.getColonyById(id).orElseThrow());
            assertEquals(site, colony.getLocation());
            assertEquals(List.of(founder), colony.getUnitsInsideColony());
            assertEquals(humanBefore, humanUnits(game.model));
        }
    }

    @Test
    void diskContinuationDuringTravelReconstructsTheSameNextStepAndFinishes()
            throws Exception {
        final GameModelPo saved = expedition();
        final Location position;
        final java.util.Optional<Location> expectedNext;
        try (Game game = new Game(load(saved))) {
            // France has already acted before the initial Dutch turn.
            assertTrue(game.model.getColonies().isEmpty());
            assertTrue(game.model.getUnitById(4).isAtCargoSlot());
            position = game.model.getUnitById(5).getLocation();
            assertNotEquals(Location.of(35, 10), position);
            expectedNext = FirstColonyPlanner.nextStep(game.model,
                    game.model.getUnitById(4));
            saved.setModel(game.model.save());
            new GameModelPoDao().saveToFile(
                    directory.resolve("travelling.microcol").toString(), saved);
        }
        final GameModelPo restored = new GameModelPoDao().loadFromFile(
                directory.resolve("travelling.microcol").toFile());
        try (Game game = new Game(load(restored))) {
            assertEquals(position, game.model.getUnitById(5).getLocation());
            assertEquals(expectedNext, FirstColonyPlanner.nextStep(game.model,
                    game.model.getUnitById(4)));
            final Colony colony = reachColony(game);
            assertEquals(List.of(game.model.getUnitById(4)),
                    colony.getUnitsInsideColony());
            assertEquals(1, game.model.save().getNextColonyId());
        }
    }

    @Test
    void enclosedFleetEndsEachRealTurnWithoutMovingOrCreatingAColony()
            throws Exception {
        final GameModelPo data = expedition();
        final java.util.Map<Location, org.microcol.model.TerrainType> terrain = data
                .getModel().getMap().getTerrainMap();
        Location.of(35, 10).getNeighbors().forEach(
                at -> terrain.put(at, org.microcol.model.TerrainType.MOUNTAIN));
        data.getModel().getMap().setTerrainType(terrain);
        try (Game game = new Game(load(data))) {
            final String humanBefore = humanUnits(game.model);
            game.nextTurn();
            game.nextTurn();
            assertEquals(Location.of(35, 10),
                    game.model.getUnitById(5).getLocation());
            assertTrue(game.model.getUnitById(4).isAtCargoSlot());
            assertTrue(game.model.getColonies().isEmpty());
            assertEquals(0, game.model.save().getNextColonyId());
            assertEquals(humanBefore, humanUnits(game.model));
        }
    }

    @Test
    void humanOccupationDuringTravelMakesTheNextTurnChooseAnotherSite()
            throws Exception {
        try (Game game = new Game(load(expedition()))) {
            // The rival has travelled north; occupy its selected shore before
            // the landing turn.
            game.nextTurn();
            assertEquals(Location.of(23, 4),
                    game.model.getUnitById(5).getLocation());
            game.model.getUnitById(0).placeToLocation(COLONY_SITE);
            final String humanBefore = humanUnits(game.model);
            final Unit founder = game.model.getUnitById(4);
            while (game.model.getCalendar().getNumberOfPlayedTurns() < 8
                    && game.model.getColonies().isEmpty()) {
                game.nextTurn();
                if (founder.isAtPlaceLocation()) {
                    assertNotEquals(COLONY_SITE, founder.getLocation());
                }
            }
            assertEquals(1, game.model.getColonies().size());
            final Colony colony = game.model.getColonies().get(0);
            assertNotEquals(COLONY_SITE, colony.getLocation());
            assertTrue(game.model.getMap()
                    .getTerrainTypeAt(colony.getLocation()).isLand());
            assertTrue(colony.getLocation().getNeighbors().stream()
                    .filter(game.model.getMap()::isValid)
                    .anyMatch(at -> game.model.getMap().getTerrainTypeAt(at)
                            .isSea()));
            assertEquals(List.of(founder), colony.getUnitsInsideColony());
            assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getNetProduction() >= 0);
            assertEquals(humanBefore, humanUnits(game.model));
        }
    }

    private static final Gson GSON = new Gson();

    private static int stock(final Colony colony, final GoodsType goods) {
        return colony.getWarehouse().getGoods(goods).getAmount();
    }

    private String franceUnits(final Model model) {
        return GSON.toJson(model.getPlayerByName("France").getAllUnits()
                .stream().map(Unit::save).collect(Collectors.toList()));
    }

    /** A fresh ordinary colonist standing at the given square. */
    private UnitPo spare(final UnitPo template, final int id,
            final String owner, final Location at) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(UnitType.COLONIST);
        unit.setOwnerId(owner);
        unit.setAction(template.getAction());
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
        // The saved allowance is mandatory; a fresh spare starts exhausted.
        unit.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        return unit;
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    private static ColonyPo colonyPo(final GameModelPo data) {
        return data.getModel().getColonies().get(0);
    }

    private static void setStartingFood(final GameModelPo data,
            final int amount) {
        final java.util.Map<String, Integer> stock = new java.util.HashMap<>(
                colonyPo(data).getColonyWarehouse());
        stock.put("CORN", amount);
        colonyPo(data).setColonyWarehouse(stock);
    }

    /** Colony squares that are land, in saved order. */
    private static List<ColonyFieldPo> landFields(final GameModelPo data) {
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        return colonyPo(data).getColonyFields().stream()
                // The saved map is sparse: a missing square is ocean.
                .filter(field -> terrain
                        .getOrDefault(COLONY_SITE.add(field.getDirection()),
                                TerrainType.OCEAN)
                        .isLand())
                .collect(Collectors.toList());
    }

    private static Location tile(final ColonyFieldPo field) {
        return COLONY_SITE.add(field.getDirection());
    }

    private static void setTerrain(final GameModelPo data, final Location at,
            final TerrainType type) {
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(at, type);
        data.getModel().getMap().setTerrainType(terrain);
    }

    /** Valid saved workplace: the founder works in the first town-hall slot. */
    private static void moveFounderIndoors(final GameModelPo data) {
        colonyPo(data).getColonyFields().stream()
                .filter(field -> Integer.valueOf(4).equals(field.getWorkerId()))
                .forEach(field -> {
                    field.setWorkerId(null);
                    field.setProducedGoodsType(null);
                });
        colonyPo(data).getConstructions().stream()
                .filter(construction -> construction
                        .getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow().getSlots().get(0).setWorkerId(4);
        final UnitPo founder = unitPo(data, 4);
        founder.setPlaceColonyField(null);
        founder.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
    }

    /** Put a saved spare colonist to work on the given colony square. */
    private static void workSpareAt(final GameModelPo data, final int id,
            final ColonyFieldPo field, final GoodsType goods) {
        field.setWorkerId(id);
        field.setProducedGoodsType(goods);
        final UnitPo unit = unitPo(data, id);
        unit.setPlaceMap(null);
        unit.setPlaceColonyField(new PlaceColonyFieldPo());
    }

    private static Colony fieldOwner(final Model model, final Unit worker) {
        return model.getColonies().stream().filter(
                colony -> colony.getUnitsInsideColony().contains(worker))
                .findFirst().orElse(null);
    }

    private static Location fieldOf(final Colony colony, final Unit worker) {
        return colony.getColonyFields().stream()
                .filter(field -> field.getUnit().orElse(null) == worker)
                .map(field -> field.getLocation()).findFirst().orElse(null);
    }

    /**
     * Scoped reference: the original expedition founds its colony, then spare
     * colonists stand in the colony square and the land around it is set to the
     * given terrain. The result is saved to disk and reloaded.
     */
    private GameModelPo foundedWithSpares(final int spares,
            final TerrainType around, final String file,
            final java.util.function.Consumer<GameModelPo> shape)
            throws Exception {
        final GameModelPo saved = expedition();
        try (Game game = new Game(load(expedition()))) {
            reachColony(game);
            saved.setModel(game.model.save());
        }
        final UnitPo founder = unitPo(saved, 4);
        final List<UnitPo> units = new ArrayList<>(saved.getModel().getUnits());
        for (int i = 0; i < spares; i++) {
            units.add(spare(founder, 6 + i, "France", COLONY_SITE));
        }
        saved.getModel().setUnits(units);
        for (final ColonyFieldPo field : landFields(saved)) {
            setTerrain(saved, tile(field), around);
        }
        saved.getModel().getMap().setTrees(java.util.Collections.emptySet());
        shape.accept(saved);
        new GameModelPoDao().saveToFile(directory.resolve(file).toString(),
                saved);
        return new GameModelPoDao()
                .loadFromFile(directory.resolve(file).toFile());
    }

    private GameModelPo foundedWithSpares(final int spares,
            final TerrainType around, final String file) throws Exception {
        return foundedWithSpares(spares, around, file, data -> {
        });
    }

    @Test
    void tenRealTurnsAfterFoundingKeepTheOnlySettlerFedWithoutInventedStocks()
            throws Exception {
        try (Game game = new Game(load(expedition()))) {
            final Colony colony = reachColony(game);
            final Unit founder = game.model.getUnitById(4);
            final int foodBefore = stock(colony, GoodsType.CORN);
            final int netBefore = colony.getGoodsStats()
                    .getStatsByType(GoodsType.CORN).getNetProduction();

            // Ten more real robot turns settle only what the resident makes.
            for (int turn = 0; turn < 10; turn++) {
                game.nextTurn();
            }
            assertSame(colony,
                    game.model.getColonyById(colony.getId()).orElseThrow());
            assertEquals(List.of(founder), colony.getUnitsInsideColony());
            assertTrue(stock(colony, GoodsType.CORN) >= foodBefore);
            if (netBefore > 0) {
                assertTrue(stock(colony, GoodsType.CORN) > foodBefore);
            }
            assertEquals(0, stock(colony, GoodsType.LUMBER));
        }
    }

    @Test
    void spareWorkersFeedTheColonyThenCutLumberAcrossSaveAndLoad()
            throws Exception {
        // Lumber exists only in forest: the first land square stays bare
        // grassland for food, every other one is forested grassland.
        final GameModelPo restored = foundedWithSpares(2, TerrainType.GRASSLAND,
                "spares.microcol",
                saved -> saved.getModel().getMap()
                        .setTrees(landFields(saved).stream().skip(1)
                                .map(FirstColonyJourneyTest::tile)
                                .collect(Collectors.toSet())));
        final String humanBefore;
        final String franceMidway;
        final int foodMidway;
        final int lumberMidway;
        try (Game game = new Game(load(restored))) {
            humanBefore = humanUnits(game.model);
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            assertEquals(1, colony.getUnitsInsideColony().size());
            for (int turn = 0; turn < 5; turn++) {
                game.nextTurn();
            }
            foodMidway = stock(colony, GoodsType.CORN);
            lumberMidway = stock(colony, GoodsType.LUMBER);
            franceMidway = franceUnits(game.model);
            restored.setModel(game.model.save());
            new GameModelPoDao().saveToFile(
                    directory.resolve("spares-midway.microcol").toString(),
                    restored);
        }

        // Continue from disk with the same workers and stocks, then finish.
        final GameModelPo reloaded = new GameModelPoDao().loadFromFile(
                directory.resolve("spares-midway.microcol").toFile());
        try (Game game = new Game(load(reloaded))) {
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            assertEquals(franceMidway, franceUnits(game.model));
            assertEquals(foodMidway, stock(colony, GoodsType.CORN));
            assertEquals(lumberMidway, stock(colony, GoodsType.LUMBER));
            for (int turn = 0; turn < 5; turn++) {
                game.nextTurn();
            }
            assertEquals(1, game.model.getColonies().size());
            assertTrue(colony.getUnitsInsideColony()
                    .containsAll(List.of(game.model.getUnitById(4),
                            game.model.getUnitById(6),
                            game.model.getUnitById(7))));
            assertTrue(stock(colony, GoodsType.CORN) > 0);
            assertTrue(stock(colony, GoodsType.LUMBER) > 0);
            assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getNetProduction() >= 0);
            assertEquals(humanBefore, humanUnits(game.model));
        }
    }

    @Test
    void anOrdinarySpareWorkerReallyJoinsTheColonyOnALegalField()
            throws Exception {
        final GameModelPo data = foundedWithSpares(1, TerrainType.GRASSLAND,
                "ordinary.microcol");
        try (Game game = new Game(load(data))) {
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            final Unit spare = game.model.getUnitById(6);
            assertTrue(spare.isAtPlaceLocation());

            // One real France turn places the idle colonist on a free field.
            game.nextTurn();
            assertTrue(colony.getUnitsInsideColony().contains(spare));
            assertTrue(spare.isAtPlaceColonyField());
            assertNotEquals(fieldOf(colony, game.model.getUnitById(4)),
                    fieldOf(colony, spare));
        }
    }

    @Test
    void aShortOfFoodWorkerMovesToAStrictlyBetterLegalFoodField()
            throws Exception {
        final Location[] better = new Location[1];
        final GameModelPo data = foundedWithSpares(1, TerrainType.MOUNTAIN,
                "better.microcol", saved -> {
                    // The owner update settles food before the AI can reassign.
                    setStartingFood(saved, 20);
                    // Founder: poor tundra corn. Spare: lumber on tundra. Free
                    // grassland offers more corn than the founder makes.
                    final List<ColonyFieldPo> land = landFields(saved);
                    setTerrain(saved, tile(land.get(0)), TerrainType.TUNDRA);
                    setTerrain(saved, tile(land.get(1)), TerrainType.TUNDRA);
                    setTerrain(saved, tile(land.get(2)), TerrainType.GRASSLAND);
                    // The spare's tundra is forested: only forest has lumber.
                    saved.getModel().getMap()
                            .setTrees(java.util.Set.of(tile(land.get(1))));
                    better[0] = tile(land.get(2));
                    land.forEach(field -> {
                        if (Integer.valueOf(4).equals(field.getWorkerId())) {
                            field.setWorkerId(null);
                            field.setProducedGoodsType(null);
                        }
                    });
                    land.get(0).setWorkerId(4);
                    land.get(0).setProducedGoodsType(GoodsType.CORN);
                    workSpareAt(saved, 6, land.get(1), GoodsType.LUMBER);
                });
        try (Game game = new Game(load(data))) {
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            final Unit founder = game.model.getUnitById(4);
            assertNotEquals(better[0], fieldOf(colony, founder));
            assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getNetProduction() < 0);

            game.nextTurn();
            assertEquals(better[0], fieldOf(colony, founder));
            assertSame(colony, fieldOwner(game.model, founder));
            assertSame(colony,
                    fieldOwner(game.model, game.model.getUnitById(6)));
        }
    }

    @Test
    void anIndoorWorkerMovesToFoodWhenTheColonyHasNone() throws Exception {
        final GameModelPo data = foundedWithSpares(0, TerrainType.GRASSLAND,
                "indoor.microcol", saved -> {
                    moveFounderIndoors(saved);
                    // Keep the resident alive until its first job decision.
                    setStartingFood(saved, 20);
                });
        try (Game game = new Game(load(data))) {
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            final Unit founder = game.model.getUnitById(4);
            assertTrue(founder.isAtPlaceConstructionSlot());

            game.nextTurn();
            assertTrue(founder.isAtPlaceColonyField());
            assertEquals(GoodsType.CORN,
                    founder.getPlaceColonyField().getProducedGoodsType());
            assertSame(colony, fieldOwner(game.model, founder));
        }
    }

    @Test
    void aForeignOccupiedFieldIsNeverTakenAndTheForeignWorkerIsUntouched()
            throws Exception {
        final GameModelPo data = foundedWithSpares(0, TerrainType.MOUNTAIN,
                "foreign.microcol", saved -> {
                    moveFounderIndoors(saved);
                    // This checks occupied-field rejection, not starvation.
                    setStartingFood(saved, 20);
                    // The only food square carries a Dutch colonist.
                    final ColonyFieldPo only = landFields(saved).get(0);
                    setTerrain(saved, tile(only), TerrainType.GRASSLAND);
                    final List<UnitPo> units = new ArrayList<>(
                            saved.getModel().getUnits());
                    final UnitPo foreign = spare(unitPo(saved, 4), 8, "Dutch",
                            tile(only));
                    // A normal turn reset must not change the comparison.
                    foreign.setAvailableMoves(foreign.getType().getSpeed());
                    units.add(foreign);
                    saved.getModel().setUnits(units);
                });
        try (Game game = new Game(load(data))) {
            final String humanBefore = humanUnits(game.model);
            final Unit founder = game.model.getUnitById(4);
            for (int turn = 0; turn < 3; turn++) {
                game.nextTurn();
                assertSame(founder, game.model.getUnitById(4));
                assertTrue(founder.isAtPlaceConstructionSlot());
                assertEquals(humanBefore, humanUnits(game.model));
            }
        }
    }

    @Test
    void withoutAnyLegalFoodTheTurnsEndAndNothingIsCreated() throws Exception {
        // The founder works indoors (a valid workplace) among mountains.
        final GameModelPo barren = foundedWithSpares(2, TerrainType.MOUNTAIN,
                "barren.microcol", FirstColonyJourneyTest::moveFounderIndoors);
        try (Game game = new Game(load(barren))) {
            final String humanBefore = humanUnits(game.model);
            final Colony colony = game.model.getColonyById(0).orElseThrow();
            final int foodBefore = stock(colony, GoodsType.CORN);
            assertNull(fieldOf(colony, game.model.getUnitById(4)));

            // Each real turn returns control; no stock or unit appears.
            for (int turn = 0; turn < 10; turn++) {
                game.nextTurn();
                assertTrue(stock(colony, GoodsType.CORN) <= foodBefore);
                assertEquals(0, stock(colony, GoodsType.LUMBER));
                // Founder, two spares and the galleon; nothing is added.
                assertTrue(game.model.getPlayerByName("France").getAllUnits()
                        .size() <= 4);
            }
            assertEquals(humanBefore, humanUnits(game.model));
        }
    }

    private static final class Game implements AutoCloseable {
        private final Model model;
        private final List<AbstractRobotPlayer> robots = new ArrayList<>();
        private final BlockingQueue<Player> humanTurns = new LinkedBlockingQueue<>();

        private Game(final Model model) throws Exception {
            this.model = model;
            final AnimationManager animations = new AnimationManager(
                    new EventBus());
            for (final Player player : model.getPlayers()) {
                if (player.isKing()) {
                    robots.add(new KingRobotPlayer(model, (PlayerKing) player,
                            animations));
                } else if (player.isComputer()) {
                    robots.add(new SimpleAiPlayer(model, player, animations));
                }
            }
            model.addListener(new ModelListenerAdapter() {
                @Override
                public void onTurnStarted(final TurnStartedEvent event) {
                    if (event.getPlayer().isHuman()) {
                        humanTurns.add(event.getPlayer());
                    }
                }
            });
            model.startGame();
            awaitHumanTurn();
        }

        private void nextTurn() throws Exception {
            model.getPlayerByName("Dutch").endTurn();
            awaitHumanTurn();
        }

        private void awaitHumanTurn() throws InterruptedException {
            assertSame(model.getPlayerByName("Dutch"),
                    humanTurns.poll(5, TimeUnit.SECONDS),
                    "Every rival turn must return control to the human");
            assertSame(model.getPlayerByName("Dutch"),
                    model.getCurrentPlayer());
        }

        @Override
        public void close() {
            robots.forEach(AbstractRobotPlayer::stop);
            model.stop();
        }
    }
}
