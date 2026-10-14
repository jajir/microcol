package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.GameStoppedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.PlayerPo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class EuropeanRelationModelTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();
    private final Gson gson = new Gson();
    private final GameModelPo data = threePlayers();
    private Model model = make(data);

    private Model make(final GameModelPo saved) {
        return Model.make(saved.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private GameModelPo threePlayers() {
        final GameModelPo saved = dao
                .loadFromClassPath("/maps/free-play.microcol");
        final List<PlayerPo> players = saved.getModel().getGameManager()
                .getPlayers();
        final PlayerPo spain = gson.fromJson(gson.toJson(
                players.stream().filter(p -> p.getName().equals("France"))
                        .findFirst().orElseThrow()),
                PlayerPo.class);
        spain.setName("Spain");
        players.add(spain);
        for (final String first : List.of("Dutch", "France")) {
            final EuropeanRelationPo pair = new EuropeanRelationPo();
            pair.setFirstPlayer(first);
            pair.setSecondPlayer("Spain");
            pair.setState(EuropeanRelationState.PEACE);
            saved.getModel().getEuropeanRelations().add(pair);
        }
        saved.getModel().getGameManager().setGameStarted(true);
        saved.getModel().getGameManager().setCurrentPlayer("Dutch");
        return saved;
    }

    private Player player(final String key) {
        return model.getPlayerByName(key);
    }

    private String snapshot() {
        return gson.toJson(model.save());
    }

    private String worldWithoutRelations() {
        final JsonObject saved = gson.toJsonTree(model.save())
                .getAsJsonObject();
        saved.remove("europeanRelations");
        return gson.toJson(saved);
    }

    private void assertPair(final String first, final String second,
            final EuropeanRelationState expected) {
        assertEquals(expected,
                model.getEuropeanRelation(player(first), player(second)));
        assertEquals(expected,
                model.getEuropeanRelation(player(second), player(first)));
    }

    @Test
    void declarationAndAcceptedPeaceOnlyChangeOnePair() {
        final String world = worldWithoutRelations();
        model.declareHostilities(player("Dutch"), player("France"));
        assertPair("Dutch", "France", EuropeanRelationState.WAR);
        assertPair("Dutch", "Spain", EuropeanRelationState.PEACE);
        assertPair("France", "Spain", EuropeanRelationState.PEACE);
        assertEquals(world, worldWithoutRelations());
        model.acceptPeace(player("Dutch"), player("France"));
        assertPair("Dutch", "France", EuropeanRelationState.PEACE);
        assertPair("Dutch", "Spain", EuropeanRelationState.PEACE);
        assertPair("France", "Spain", EuropeanRelationState.PEACE);
        assertEquals(world, worldWithoutRelations());
    }

    @Test
    void staleDeclarationsAndDuplicatePeaceAreAtomic() {
        String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> model.acceptPeace(player("Dutch"), player("France")));
        assertEquals(before, snapshot());
        model.declareHostilities(player("Dutch"), player("France"));
        before = snapshot();
        assertThrows(IllegalArgumentException.class, () -> model
                .declareHostilities(player("Dutch"), player("France")));
        assertEquals(before, snapshot());
        model.acceptPeace(player("Dutch"), player("France"));
        before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> model.acceptPeace(player("Dutch"), player("France")));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void wrongTurnRejectsBothTransitionsWithoutEffects(final boolean peace) {
        model.declareHostilities(player("Dutch"), player("France"));
        final String before = snapshot();
        assertThrows(IllegalStateException.class, () -> {
            if (peace) {
                model.acceptPeace(player("France"), player("Dutch"));
            } else {
                model.declareHostilities(player("France"), player("Spain"));
            }
        });
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void invalidParticipantsRejectBothTransitionsWithoutEffects(
            final boolean peace) {
        if (peace) {
            model.declareHostilities(player("Dutch"), player("France"));
        }
        final Model otherGame = make(data);
        final Player unknown = new PlayerHuman("Unknown", false, 0, model,
                false, Map.of(), Set.of());
        final List<Player[]> invalid = new ArrayList<>();
        invalid.add(new Player[] { null, player("France") });
        invalid.add(new Player[] { player("Dutch"), null });
        invalid.add(new Player[] { player("Dutch"), player("Dutch") });
        invalid.add(new Player[] { player("Dutch"), player("Dutch's King") });
        invalid.add(new Player[] { player("Dutch"), unknown });
        invalid.add(new Player[] { unknown, player("France") });
        invalid.add(new Player[] { otherGame.getPlayerByName("Dutch"),
                player("France") });
        invalid.add(new Player[] { player("Dutch"),
                otherGame.getPlayerByName("France") });
        final String before = snapshot();
        for (final Player[] pair : invalid) {
            assertThrows(IllegalArgumentException.class, () -> {
                if (peace) {
                    model.acceptPeace(pair[0], pair[1]);
                } else {
                    model.declareHostilities(pair[0], pair[1]);
                }
            });
            assertEquals(before, snapshot());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Dutch", "France" })
    void removedParticipantCannotUseItsPreviousIdentity(
            final String removedKey) {
        final Player actor = player("Dutch");
        final Player target = player("France");
        model.declareHostilities(actor, target);
        model.getPlayerStore().getPlayers().remove(player(removedKey));
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> model.acceptPeace(actor, target));
        assertThrows(IllegalArgumentException.class,
                () -> model.declareHostilities(actor, target));
        assertEquals(before, snapshot());
    }

    @Test
    void crownCannotActEvenOnItsOwnTurn() {
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        model = make(data);
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class, () -> model
                .declareHostilities(player("Dutch's King"), player("Dutch")));
        assertThrows(IllegalArgumentException.class, () -> model
                .acceptPeace(player("Dutch's King"), player("Dutch")));
        assertEquals(before, snapshot());
    }

    @Test
    void eitherPartyCanRecordAnAcceptedTreatyOnItsOwnTurn() {
        model.declareHostilities(player("Dutch"), player("France"));
        data.setModel(model.save());
        data.getModel().getGameManager().setCurrentPlayer("France");
        model = make(data);
        final String world = worldWithoutRelations();
        model.acceptPeace(player("France"), player("Dutch"));
        assertPair("Dutch", "France", EuropeanRelationState.PEACE);
        assertEquals(world, worldWithoutRelations());
    }

    @Test
    void stoppedModelRejectsChangesBeforeNotifyingListenersButCanBeSavedAndReloaded() {
        model.declareHostilities(player("Dutch"), player("France"));
        final String before = snapshot();
        final int[] notified = { 0 };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameStopped(final GameStoppedEvent event) {
                notified[0]++;
                assertFalse(model.isGameRunning());
                assertThrows(IllegalStateException.class, () -> model
                        .acceptPeace(player("Dutch"), player("France")));
            }
        });
        model.stop();
        assertEquals(1, notified[0]);
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, () -> model
                .declareHostilities(player("Dutch"), player("Spain")));
        assertThrows(IllegalStateException.class, model::endTurn);
        assertThrows(IllegalStateException.class, model::startGame);
        assertEquals(before, snapshot());
        data.setModel(model.save());
        model = make(data);
        model.startGame();
        assertTrue(model.isGameRunning());
        assertPair("Dutch", "France", EuropeanRelationState.WAR);
        assertEquals(before, snapshot());
    }

    @Test
    void gameOverBeforeCalendarEndRejectsTransitionsAndRetainsItsResultOnDisk()
            throws Exception {
        model.declareHostilities(player("Dutch"), player("France"));
        data.setModel(model.save());
        data.getModel().getGameManager().setPlayers(
                new ArrayList<>(data.getModel().getGameManager().getPlayers()));
        data.getModel().getGameManager().getPlayers()
                .removeIf(p -> p.getWhosKingThisPlayerIs() != null);
        data.getModel().getGameManager().setCurrentPlayer("Spain");
        model = Model.make(data.getModel(),
                List.of(m -> new GameOverResult("TEST_GAME_OVER")));
        final int[] notified = { 0 };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                notified[0]++;
                assertFalse(model.isGameRunning());
                assertTrue(model.isGameFinished());
                assertEquals("Spain", model.getCurrentPlayer().getName());
                assertThrows(IllegalStateException.class, () -> model
                        .declareHostilities(player("Spain"), player("Dutch")));
            }
        });
        model.endTurn();
        assertEquals(1, notified[0]);
        assertFalse(model.getCalendar().isFinished());
        final String before = snapshot();
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeace(player("Dutch"), player("France")));
        assertThrows(IllegalStateException.class, () -> model
                .declareHostilities(player("Spain"), player("Dutch")));
        assertThrows(IllegalStateException.class, model::endTurn);
        assertEquals(before, snapshot());
        data.setModel(model.save());
        final Path file = directory.resolve("finished.microcol");
        dao.saveToFile(file.toString(), data);
        model = make(dao.loadFromFile(file.toFile()));
        assertTrue(model.isGameFinished());
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, model::startGame);
        assertEquals(before, snapshot());
    }

    @Test
    void discardedUnstartedGameCannotBeStartedLater() {
        data.getModel().getGameManager().setGameStarted(false);
        model = make(data);
        model.stop();
        assertThrows(IllegalStateException.class, model::startGame);
        assertFalse(model.isGameRunning());
    }

    @Test
    void calendarFinishedGameRejectsBothTransitions() {
        model.declareHostilities(player("Dutch"), player("France"));
        data.setModel(model.save());
        data.getModel().getCalendar().setNumberOfPlayedTurns(2000);
        model = make(data);
        assertTrue(model.isGameFinished());
        final String before = snapshot();
        assertThrows(IllegalStateException.class, () -> model
                .declareHostilities(player("Dutch"), player("Spain")));
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeace(player("Dutch"), player("France")));
        assertEquals(before, snapshot());
    }

    @Test
    void unstartedGameRejectsBothTransitionsWithoutChangingWorldOrPairs() {
        data.getModel().getGameManager().setGameStarted(false);
        data.getModel().getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.WAR);
        model = make(data);
        final String players = gson.toJson(model.getPlayers().stream()
                .map(Player::save).collect(Collectors.toList()));
        final String units = gson.toJson(model.getAllUnits().stream()
                .map(Unit::save).collect(Collectors.toList()));
        final String colonies = gson.toJson(model.getPlayers().stream()
                .flatMap(p -> model.getColonies(p).stream()).map(Colony::save)
                .collect(Collectors.toList()));
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, () -> model
                .declareHostilities(player("Dutch"), player("Spain")));
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeace(player("Dutch"), player("France")));
        assertEquals(players, gson.toJson(model.getPlayers().stream()
                .map(Player::save).collect(Collectors.toList())));
        assertEquals(units, gson.toJson(model.getAllUnits().stream()
                .map(Unit::save).collect(Collectors.toList())));
        assertEquals(colonies,
                gson.toJson(model.getPlayers().stream()
                        .flatMap(p -> model.getColonies(p).stream())
                        .map(Colony::save).collect(Collectors.toList())));
        assertPair("Dutch", "France", EuropeanRelationState.WAR);
        assertPair("Dutch", "Spain", EuropeanRelationState.PEACE);
        assertPair("France", "Spain", EuropeanRelationState.PEACE);
    }

    @Test
    void acceptedPeacePersistsWithoutEndingAnotherWar() throws Exception {
        model.declareHostilities(player("Dutch"), player("France"));
        model.declareHostilities(player("Dutch"), player("Spain"));
        model.acceptPeace(player("Dutch"), player("France"));
        final String before = snapshot();
        data.setModel(model.save());
        final Path file = directory.resolve("accepted-peace.microcol");
        dao.saveToFile(file.toString(), data);
        final byte[] saved = Files.readAllBytes(file);
        model = make(dao.loadFromFile(file.toFile()));
        model.startGame();
        assertPair("Dutch", "France", EuropeanRelationState.PEACE);
        assertPair("Dutch", "Spain", EuropeanRelationState.WAR);
        assertPair("France", "Spain", EuropeanRelationState.PEACE);
        assertEquals(before, snapshot());
        assertArrayEquals(saved, Files.readAllBytes(file));
    }

    @Test
    void renameAndReorderedDiskDataPreserveEveryPairAndOwnershipWithoutReplay()
            throws Exception {
        model.declareHostilities(player("Dutch"), player("France"));
        model.declareHostilities(player("Dutch"), player("Spain"));
        player("Dutch").setDisplayName("  Žluťoučký 🦁  ");
        player("France").setDisplayName("Žluťoučký 🦁");
        final String units = gson.toJson(model.save().getUnits());
        final String colonies = gson.toJson(model.save().getColonies());
        data.setModel(model.save());
        data.getModel().getGameManager().setPlayers(
                new ArrayList<>(data.getModel().getGameManager().getPlayers()));
        Collections.reverse(data.getModel().getGameManager().getPlayers());
        Collections.reverse(data.getModel().getEuropeanRelations());
        for (final EuropeanRelationPo row : data.getModel()
                .getEuropeanRelations()) {
            final String first = row.getFirstPlayer();
            row.setFirstPlayer(row.getSecondPlayer());
            row.setSecondPlayer(first);
        }
        final Path file = directory.resolve("renamed.microcol");
        dao.saveToFile(file.toString(), data);
        final byte[] before = Files.readAllBytes(file);
        model = make(dao.loadFromFile(file.toFile()));
        final String loaded = snapshot();
        model.startGame();
        assertEquals(loaded, snapshot());
        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        assertEquals("Žluťoučký 🦁", player("Dutch").getDisplayName());
        assertEquals("Žluťoučký 🦁", player("France").getDisplayName());
        assertPair("Dutch", "France", EuropeanRelationState.WAR);
        assertPair("Dutch", "Spain", EuropeanRelationState.WAR);
        assertPair("France", "Spain", EuropeanRelationState.PEACE);
        assertEquals(units, gson.toJson(model.save().getUnits()));
        assertEquals(colonies, gson.toJson(model.save().getColonies()));
        assertEquals(3, model.save().getEuropeanRelations().size());
        model.acceptPeace(player("Dutch"), player("France"));
        assertPair("Dutch", "Spain", EuropeanRelationState.WAR);
    }
}
