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
import org.microcol.model.GoodsType;
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
    private static final int FOUNDING_ROUND = 2;
    private static final Location COLONY_SITE = Location.of(26, 15);

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
            assertEquals(Location.of(29, 12),
                    game.model.getUnitById(5).getLocation());
            assertTrue(game.model.getUnitById(4).isAtCargoSlot());
        }
        while (game.model.getCalendar()
                .getNumberOfPlayedTurns() < FOUNDING_ROUND
                && game.model.getColonies().isEmpty()) {
            game.nextTurn();
            final int round = game.model.getCalendar().getNumberOfPlayedTurns();
            if (round == 1) {
                assertEquals(Location.of(27, 14),
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
            // The player occupies the intended shore before the rival's next
            // turn.
            game.model.getUnitById(0).placeToLocation(COLONY_SITE);
            final String humanBefore = humanUnits(game.model);
            game.nextTurn();
            final Unit founder = game.model.getUnitById(4);
            assertTrue(founder.isAtPlaceLocation());
            assertNotEquals(COLONY_SITE, founder.getLocation());
            game.nextTurn();
            assertEquals(1, game.model.getColonies().size());
            final Colony colony = game.model.getColonies().get(0);
            assertNotEquals(COLONY_SITE, colony.getLocation());
            assertEquals(List.of(founder), colony.getUnitsInsideColony());
            assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getNetProduction() >= 0);
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
