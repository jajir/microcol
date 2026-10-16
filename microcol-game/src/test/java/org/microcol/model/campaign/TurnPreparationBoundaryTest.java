package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.store.CampaignsDao;
import org.microcol.model.event.ActionWasStartedEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.NewUnitWasBornEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitArrivedToColoniesEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Exercise actual snapshot creation and the same file writer used by the UI.
 */
class TurnPreparationBoundaryTest {
    @TempDir
    Path directory;
    private final GameModelPoDao files = new GameModelPoDao();
    private final GameModelDao games = new GameModelDao(files,
            new CampaignManager(List.of(), mock(CampaignsDao.class)),
            mock(MissionFactoryManager.class));
    private final Gson gson = new Gson();

    private Model preparedFixture() {
        final GameModelPo data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        data.getModel().getColonies().get(1).getColonyWarehouse().put("CORN",
                200);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceHighSeas()
                .setRemainigTurns(1);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().getPlaceHighSeas().setPosition(2);
        final java.util.Set<Location> fields = data.getModel().getMap()
                .getFieldSet();
        fields.remove(Location.of(24, 14));
        data.getModel().getMap().setFields(fields);
        final Model model = make(data.getModel());
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .getUnitById(16);
        pioneer.placeToLocation(Location.of(24, 14));
        pioneer.setTools(100);
        pioneer.setActionType(UnitActionType.plowField);
        ((UnitActionPlowField) pioneer.getAction()).setRemainingTurns(1);
        return model;
    }

    private Model make(final ModelPo state) {
        return Model.make(state,
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private GameModel envelope(final Model model) {
        final Campaign campaign = mock(Campaign.class);
        when(campaign.getName()).thenReturn(CampaignNames.defaultCampaign);
        final MissionName name = mock(MissionName.class);
        when(name.getName()).thenReturn("boundary-test");
        final Mission<?> mission = mock(Mission.class);
        when(mission.saveGoals()).thenReturn(Map.of());
        return new GameModel(campaign, new CampaignMission(name, 0), mission,
                model);
    }

    private void save(final Model model, final Path file) {
        games.saveToFile(file.toString(), envelope(model));
    }

    private JsonElement snapshot(final Model model) {
        return gson.toJsonTree(model.save());
    }

    private void atStage(final Model model, final String stage,
            final Runnable callback) {
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitArraviedToColonies(
                    final UnitArrivedToColoniesEvent event) {
                if (stage.equals("arrival"))
                    callback.run();
            }

            @Override
            public void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
                if (stage.equals("birth"))
                    callback.run();
            }

            @Override
            public void onActionWasStarted(final ActionWasStartedEvent event) {
                if (stage.equals("work"))
                    callback.run();
            }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "arrival", "birth", "work" })
    void reentrantSnapshotsRejectBeforeReplacingThePreviousSave(
            final String stage) throws Exception {
        final Model model = preparedFixture();
        final Path file = directory.resolve("valid.microcol");
        save(model, file);
        final byte[] before = Files.readAllBytes(file);
        final AtomicInteger observed = new AtomicInteger();
        atStage(model, stage, () -> {
            observed.incrementAndGet();
            assertFalse(model.isGameRunning());
            assertThrows(IllegalStateException.class, model::save);
            assertThrows(IllegalStateException.class, () -> save(model, file));
            assertThrows(IllegalStateException.class, model::endTurn);
            assertThrows(IllegalStateException.class, model::startGame);
        });
        model.endTurn();
        assertEquals(1, observed.get());
        assertTrue(model.isGameRunning());
        assertArrayEquals(before, Files.readAllBytes(file));
        final JsonElement completed = snapshot(model);
        save(model, file);
        final Model resumed = make(
                files.loadFromFile(file.toFile()).getModel());
        resumed.startGame();
        resumed.startGame();
        assertEquals(completed, snapshot(resumed));
    }

    @Test
    void anotherThreadsSnapshotWaitsForACompletePreparedTurn()
            throws Exception {
        final Model model = preparedFixture();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch saving = new CountDownLatch(1);
        atStage(model, "birth", () -> {
            entered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError(exception);
            }
        });
        final ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            final Future<?> transition = threads.submit(model::endTurn);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            final Future<ModelPo> pendingSave = threads.submit(() -> {
                saving.countDown();
                return model.save();
            });
            assertTrue(saving.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class,
                    () -> pendingSave.get(100, TimeUnit.MILLISECONDS));
            release.countDown();
            transition.get(5, TimeUnit.SECONDS);
            assertEquals(snapshot(model),
                    gson.toJsonTree(pendingSave.get(5, TimeUnit.SECONDS)));
            assertEquals("Dutch", model.getCurrentPlayer().getName());
            assertEquals(11, model.getCalendar().getNumberOfPlayedTurns());
            assertEquals(UnitActionType.noAction,
                    model.getUnitById(16).getAction().getType());
        } finally {
            release.countDown();
            threads.shutdownNow();
            assertTrue(threads.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "arrival", "birth", "work" })
    void failedPreparationCannotActOrSaveAndReloadRecovers(final String stage)
            throws Exception {
        final Model model = preparedFixture();
        final Path file = directory.resolve("previous.microcol");
        save(model, file);
        final byte[] before = Files.readAllBytes(file);
        final AtomicInteger freshNotifications = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (event.isFreshStart())
                    freshNotifications.incrementAndGet();
            }
        });
        atStage(model, stage, () -> {
            throw new IllegalStateException("Injected preparation failure");
        });
        assertEquals("Injected preparation failure",
                assertThrows(IllegalStateException.class, model::endTurn)
                        .getMessage());
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, model::startGame);
        assertThrows(IllegalStateException.class, model::endTurn);
        assertThrows(IllegalStateException.class, () -> save(model, file));
        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals(0, freshNotifications.get());
        final Model recovered = make(
                files.loadFromFile(file.toFile()).getModel());
        final Model expected = preparedFixture();
        expected.endTurn();
        recovered.startGame();
        recovered.endTurn();
        assertTrue(recovered.isGameRunning());
        assertEquals(snapshot(expected), snapshot(recovered));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void canceledBeforeEndTurnRejectsRecursionAndRetainsTheSaveBoundary() {
        final Model model = preparedFixture();
        final JsonElement before = snapshot(model);
        final AtomicInteger calls = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
                calls.incrementAndGet();
                assertThrows(IllegalStateException.class, model::endTurn);
                assertThrows(IllegalStateException.class, model::startGame);
                assertEquals(before, snapshot(model));
                event.stopEventExecution();
            }
        });
        model.endTurn();
        model.endTurn();
        assertEquals(2, calls.get());
        assertTrue(model.isGameRunning());
        assertEquals(before, snapshot(model));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void freshAndResumedSetupRejectSnapshotsAndRetainFailure(
            final boolean fresh) {
        final ModelPo state = preparedFixture().save();
        state.getGameManager().setGameStarted(!fresh);
        if (fresh)
            state.getGameManager().setCurrentPlayer(null);
        final Model model = make(state);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameStarted(final GameStartedEvent event) {
                assertFalse(model.isGameRunning());
                assertThrows(IllegalStateException.class, model::save);
                throw new IllegalStateException("Injected setup failure");
            }
        });
        assertEquals("Injected setup failure",
                assertThrows(IllegalStateException.class, model::startGame)
                        .getMessage());
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, model::save);
        assertThrows(IllegalStateException.class, model::startGame);
    }

    @Test
    void roundStartedIsStillInsidePreparationAndItsFailureCannotBeSaved() {
        final Model model = preparedFixture();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onRoundStarted(final RoundStartedEvent event) {
                assertFalse(model.isGameRunning());
                assertThrows(IllegalStateException.class, model::save);
                throw new IllegalStateException("Injected round setup failure");
            }
        });
        assertEquals("Injected round setup failure",
                assertThrows(IllegalStateException.class, model::endTurn)
                        .getMessage());
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, model::save);
    }

    @Test
    void completedGameOverIsStillASaveBoundary() throws Exception {
        final Model model = Model.make(preparedFixture().save(), List
                .of(ignored -> new GameOverResult("Boundary test finished")));
        final Path file = directory.resolve("finished.microcol");
        final AtomicInteger finished = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                finished.incrementAndGet();
                assertFalse(model.isGameRunning());
                assertTrue(model.isGameFinished());
                save(model, file);
            }
        });
        model.endTurn();
        assertEquals(1, finished.get());
        final Model restored = make(
                files.loadFromFile(file.toFile()).getModel());
        assertTrue(restored.isGameFinished());
        assertEquals(snapshot(model), snapshot(restored));
    }
}
