package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnFinishedEvent;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.PlayerPo;

import com.google.gson.Gson;

class NationalTurnOrderTest {
    @TempDir
    Path directory;
    private final Gson gson = new Gson();
    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo fixture(final List<String> nations,
            final List<String> crowned) {
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        final List<PlayerPo> players = data.getModel().getGameManager()
                .getPlayers();
        final PlayerPo nationTemplate = players.stream()
                .filter(p -> p.getName().equals("France")).findFirst()
                .orElseThrow();
        final PlayerPo crownTemplate = players.stream()
                .filter(p -> p.getWhosKingThisPlayerIs() != null).findFirst()
                .orElseThrow();
        players.clear();
        for (final String name : nations) {
            final PlayerPo nation = gson.fromJson(gson.toJson(nationTemplate),
                    PlayerPo.class);
            nation.setName(name);
            nation.setComputer(!name.equals("Dutch"));
            players.add(nation);
        }
        for (int index = 0; index < crowned.size(); index++) {
            final PlayerPo crown = gson.fromJson(gson.toJson(crownTemplate),
                    PlayerPo.class);
            crown.setName("Crown " + index);
            crown.setWhosKingThisPlayerIs(crowned.get(index));
            players.add(0, crown);
        }
        data.getModel().getUnits().clear();
        data.getModel().getColonies().clear();
        data.getModel().getTurnEvents().clear();
        data.getModel().getEuropeanRelations().clear();
        for (int first = 0; first < nations.size(); first++) {
            for (int second = first + 1; second < nations.size(); second++) {
                final EuropeanRelationPo pair = new EuropeanRelationPo();
                pair.setFirstPlayer(nations.get(first));
                pair.setSecondPlayer(nations.get(second));
                pair.setState(EuropeanRelationState.PEACE);
                data.getModel().getEuropeanRelations().add(pair);
            }
        }
        data.getModel().getGameManager().setGameStarted(false);
        data.getModel().getGameManager().setCurrentPlayer(null);
        return data;
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private List<String> names(final Model model) {
        return model.getPlayers().stream().map(Player::getName)
                .collect(Collectors.toList());
    }

    @Test
    void orderedRoundIncludesScriptedActorsAndAssociatedCrowns() {
        final Model model = make(fixture(
                List.of("Zulu", "Dutch", "Spain", "Alpha", "France", "England"),
                List.of("Dutch", "Alpha", "England", "France")));
        final List<String> storedOrder = names(model);
        final List<String> expected = List.of("England", "France", "Spain",
                "Dutch", "Alpha", "Zulu", "Crown 2", "Crown 3", "Crown 0",
                "Crown 1");
        model.startGame();
        for (final String name : expected) {
            assertEquals(name, model.getCurrentPlayer().getName());
            assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
            model.endTurn();
        }
        assertEquals("England", model.getCurrentPlayer().getName());
        assertEquals(1, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(storedOrder, names(model));
    }

    @Test
    void absentNationsDoNotGetDummyTurns() {
        final Model model = make(
                fixture(List.of("Dutch", "France"), List.of("Dutch")));
        model.startGame();
        assertEquals("France", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals("Crown 0", model.getCurrentPlayer().getName());
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
        model.endTurn();
        assertEquals("France", model.getCurrentPlayer().getName());
        assertEquals(1, model.getCalendar().getNumberOfPlayedTurns());
    }

    @ParameterizedTest
    @ValueSource(strings = { "England", "France", "Spain", "Dutch" })
    void humanSelectionAndDisplayNamesDoNotChangeNationOrder(
            final String human) {
        final GameModelPo data = fixture(
                List.of("Dutch", "Spain", "France", "England"), List.of());
        data.getModel().getGameManager().getPlayers().forEach(
                player -> player.setComputer(!player.getName().equals(human)));
        final Model model = make(data);
        model.getPlayers().forEach(player -> player.setDisplayName(
                "Display " + (10 - names(model).indexOf(player.getName()))));
        model.startGame();
        for (final String nation : List.of("England", "France", "Spain",
                "Dutch")) {
            assertEquals(nation, model.getCurrentPlayer().getName());
            model.endTurn();
        }
        assertEquals(1, model.getCalendar().getNumberOfPlayedTurns());
    }

    @Test
    void saveAndContinuePreserveTheCurrentActorDespiteReorderedStorage() {
        final GameModelPo data = fixture(
                List.of("Dutch", "Spain", "France", "England"),
                List.of("Dutch"));
        final Model model = make(data);
        model.startGame();
        model.endTurn();
        model.endTurn();
        assertEquals("Spain", model.getCurrentPlayer().getName());
        data.setModel(model.save());
        final List<PlayerPo> savedPlayers = new ArrayList<>(
                data.getModel().getGameManager().getPlayers());
        Collections.reverse(savedPlayers);
        data.getModel().getGameManager().setPlayers(savedPlayers);
        final Path file = directory.resolve("turn.microcol");
        dao.saveToFile(file.toString(), data);
        final Model resumed = make(dao.loadFromFile(file.toFile()));
        final String saved = gson.toJson(resumed.save());
        final AtomicInteger roundNotifications = new AtomicInteger();
        resumed.addListener(new ModelListenerAdapter() {
            @Override
            public void onRoundStarted(final RoundStartedEvent event) {
                roundNotifications.incrementAndGet();
            }
        });
        resumed.startGame();
        resumed.startGame();
        assertEquals(2, roundNotifications.get());
        assertEquals(saved, gson.toJson(resumed.save()));
        assertEquals("Spain", resumed.getCurrentPlayer().getName());
        resumed.endTurn();
        assertEquals("Dutch", resumed.getCurrentPlayer().getName());
        assertEquals(0, resumed.getCalendar().getNumberOfPlayedTurns());
    }

    @Test
    void eachTransitionUsesAFreshCopyOfTheRegisteredRoster() {
        final Model model = make(fixture(
                List.of("Dutch", "Spain", "France", "England"), List.of()));
        model.startGame();
        final Player spain = model.getPlayerByName("Spain");
        model.getPlayerStore().getPlayers().remove(spain);
        Collections.reverse(model.getPlayerStore().getPlayers());
        model.endTurn();
        assertEquals("France", model.getCurrentPlayer().getName());
        model.getPlayerStore().getPlayers().add(0, spain);
        final List<Player> stored = new ArrayList<>(model.getPlayers());
        model.endTurn();
        assertSame(spain, model.getCurrentPlayer());
        assertEquals(stored, model.getPlayers());
    }

    @Test
    void replacingCurrentActorWithAnEqualForeignObjectRejectsBeforeFinish() {
        final GameModelPo data = fixture(List.of("Dutch", "England"),
                List.of());
        final Model model = make(data);
        final Model other = make(data);
        model.startGame();
        other.startGame();
        final Player current = model.getCurrentPlayer();
        final Player foreign = other.getCurrentPlayer();
        assertEquals(current, foreign);
        assertNotSame(current, foreign);
        model.getPlayerStore().getPlayers()
                .set(model.getPlayers().indexOf(current), foreign);
        final AtomicInteger finished = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnFinished(final TurnFinishedEvent event) {
                finished.incrementAndGet();
            }
        });
        assertThrows(IllegalStateException.class, model::endTurn);
        assertSame(current, model.getCurrentPlayer());
        assertEquals(0, finished.get());
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
    }

    @Test
    void sharedCurrentGuardRejectsAnUnregisteredCurrentObject() {
        final GameModelPo data = fixture(List.of("Dutch"), List.of());
        final Model model = make(data);
        final Model other = make(data);
        model.startGame();
        other.startGame();
        final Player current = model.getCurrentPlayer();
        model.getPlayerStore().getPlayers().set(0, other.getCurrentPlayer());
        assertThrows(IllegalStateException.class,
                () -> model.checkCurrentPlayer(current));
    }

    @Test
    void emptyRosterRejectsStartWithoutMarkingTheGameStarted() {
        final Model model = make(fixture(List.of("Dutch"), List.of()));
        model.getPlayerStore().getPlayers().clear();
        final AtomicInteger started = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameStarted(final GameStartedEvent event) {
                started.incrementAndGet();
            }
        });
        assertThrows(IllegalStateException.class, model::startGame);
        assertFalse(model.isGameStarted());
        assertEquals(0, started.get());
    }
}
