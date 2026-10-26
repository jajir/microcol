package org.microcol.gui.event.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignModule;
import org.microcol.model.campaign.GameModelDao;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.store.CampaignsDao;
import org.microcol.model.campaign.store.CampaignsPo;
import org.microcol.model.store.PlaceMapPo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

class GameLoadRejectionTest {
    @TempDir
    Path directory;
    private final GameModelController current = mock(GameModelController.class);
    private final GameModelPoDao files = new GameModelPoDao();
    private GameController controller;
    private DiagnosticLog diagnostics;
    private File source;
    private GameModelPo data;

    @BeforeEach
    void prepare() {
        final CampaignsDao campaigns = mock(CampaignsDao.class);
        when(campaigns.load()).thenReturn(new CampaignsPo());
        final Injector injector = Guice.createInjector(new CampaignModule(),
                binder -> binder.bind(CampaignsDao.class)
                        .toInstance(campaigns));
        diagnostics = new DiagnosticLog(directory.resolve("logs"),
                "test-build");
        controller = new GameController(current,
                injector.getInstance(GameModelDao.class),
                injector.getInstance(CampaignManager.class), new EventBus(),
                diagnostics);
        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        source = directory.resolve("game.microcol").toFile();
        files.saveToFile(source.getAbsolutePath(), data);
    }

    @Test
    void rejectedDataCannotReplaceTheCurrentGameOrRewriteTheFile()
            throws Exception {
        data.getModel().getColonies().get(1)
                .setName(data.getModel().getColonies().get(0).getName());
        files.saveToFile(source.getAbsolutePath(), data);
        final byte[] before = Files.readAllBytes(source.toPath());

        assertFalse(controller.loadModelFromFile(source));

        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
        assertArrayEquals(before, Files.readAllBytes(source.toPath()));
    }

    @Test
    void malformedAndEmptyJsonPreserveTheSourceAndAllowValidRetry()
            throws Exception {
        final byte[] valid = Files.readAllBytes(source.toPath());
        for (final String broken : new String[] { "{", "", "null", "{}" }) {
            Files.writeString(source.toPath(), broken);
            final byte[] before = Files.readAllBytes(source.toPath());
            assertFalse(controller.loadModelFromFile(source));
            assertArrayEquals(before, Files.readAllBytes(source.toPath()));
        }
        verify(current, times(4)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());

        Files.write(source.toPath(), valid);
        assertTrue(controller.loadModelFromFile(source));
        verify(current).setAndStartModel(any());
    }

    @Test
    void rejectedLoadIsRecordedWithoutMessagesOrSourceLocation()
            throws Exception {
        Files.writeString(source.toPath(), "{");

        assertFalse(controller.loadModelFromFile(source));

        final String record = Files.readString(diagnostics.getActiveFile());
        assertTrue(record.contains("event=LOAD_FAILED"));
        assertTrue(record.contains("build=test-build"));
        assertFalse(record.contains("column"), "Parser message was recorded");
        assertFalse(record.contains(directory.toString()),
                "Source location was recorded");
    }

    @Test
    void missingAndNonFileSourcesShowFailureWithoutStarting() {
        assertFalse(controller.loadModelFromFile(
                directory.resolve("missing.microcol").toFile()));
        assertFalse(controller.loadModelFromFile(directory.toFile()));
        verify(current, times(2)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void missingIdentityAndInvalidReferencesDoNotReplaceTheGame()
            throws Exception {
        data.getModel().getGameManager().getPlayers().get(0).setName(null);
        files.saveToFile(source.getAbsolutePath(), data);
        assertRejectedSource();

        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getUnits().get(0).setOwnerId("unknown participant");
        files.saveToFile(source.getAbsolutePath(), data);
        assertRejectedSource();

        verify(current, times(2)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void missingNestedLocationRejectsBeforeReplacingTheCurrentGame()
            throws Exception {
        data.getModel().getUnits().get(0).setPlaceMap(new PlaceMapPo());
        files.saveToFile(source.getAbsolutePath(), data);

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void nullShipCargoSlotRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        data = files.loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getType().isShip()
                        && !unit.getCargo().getSlots().isEmpty())
                .findFirst().orElseThrow().getCargo().getSlots().set(0, null);
        files.saveToFile(source.getAbsolutePath(), data);

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void truncatedTerrainRowsRejectWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(Files.readString(source.toPath())).getAsJsonObject();
        final com.google.gson.JsonArray rows = root.getAsJsonObject("model")
                .getAsJsonObject("map").getAsJsonArray("tiles");
        rows.remove(rows.size() - 1);
        Files.writeString(source.toPath(), root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void nullTerrainRowRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(Files.readString(source.toPath())).getAsJsonObject();
        root.getAsJsonObject("model").getAsJsonObject("map")
                .getAsJsonArray("tiles")
                .set(1, com.google.gson.JsonNull.INSTANCE);
        Files.writeString(source.toPath(), root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void shortTerrainRowRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(Files.readString(source.toPath())).getAsJsonObject();
        root.getAsJsonObject("model").getAsJsonObject("map")
                .getAsJsonArray("tiles")
                .set(1, new com.google.gson.JsonPrimitive("row-0001:_"));
        Files.writeString(source.toPath(), root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void unknownCampaignAndMissionDoNotReplaceTheGame() throws Exception {
        data.setCampaignName("unknown campaign");
        files.saveToFile(source.getAbsolutePath(), data);
        assertRejectedSource();

        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.setMissionName("unknown mission");
        files.saveToFile(source.getAbsolutePath(), data);
        assertRejectedSource();

        verify(current, times(2)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    private void assertRejectedSource() throws Exception {
        final byte[] before = Files.readAllBytes(source.toPath());
        assertFalse(controller.loadModelFromFile(source));
        assertArrayEquals(before, Files.readAllBytes(source.toPath()));
    }

    @Test
    void validDataReportsSuccessAfterStartingTheLoadedGame() {
        assertTrue(controller.loadModelFromFile(source));

        verify(current).setAndStartModel(any());
        verify(current, never()).showInvalidSavedGame();
    }

    @Test
    void startupFailureIsNotMisreportedAsInvalidSavedData() {
        final IllegalArgumentException failure = new IllegalArgumentException(
                "UI startup failed");
        doThrow(failure).when(current).setAndStartModel(any());

        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> controller.loadModelFromFile(source)));

        verify(current, never()).showInvalidSavedGame();
    }

    @Test
    void unexpectedLoaderFailureIsNotSwallowed() {
        assertThrows(NullPointerException.class,
                () -> controller.loadModelFromFile(null));

        verifyZeroInteractions(current);
    }

    @Test
    void cancelledReplacementKeepsCurrentGameAndCorruptLoadNeverAsksToLeave()
            throws Exception {
        final java.util.function.BooleanSupplier leave = mock(
                java.util.function.BooleanSupplier.class);
        when(leave.getAsBoolean()).thenReturn(false);
        assertFalse(controller.loadModelFromFile(source, leave));
        verify(leave).getAsBoolean();
        verify(current, never()).setAndStartModel(any());
        clearInvocations(leave);
        Files.writeString(source.toPath(), "invalid");
        assertFalse(controller.loadModelFromFile(source, leave));
        verify(leave, never()).getAsBoolean();
    }

    @Test
    void namedSavePreservesPlaythroughAndNormalizesUnicodeName()
            throws Exception {
        assertTrue(controller.loadModelFromFile(source));
        final var loaded = org.mockito.ArgumentCaptor
                .forClass(org.microcol.model.campaign.GameModel.class);
        verify(current).setAndStartModel(loaded.capture());
        when(current.getGameModel()).thenReturn(loaded.getValue());
        controller.writeNamedGame(source, "\u2003Přístav / první\u00a0");
        final var saved = files.loadFromFile(source);
        assertEquals("Přístav / první", saved.getSaveName());
        assertEquals(loaded.getValue().getPlaythroughId(),
                saved.getPlaythroughId());
        final byte[] bytes = Files.readAllBytes(source.toPath());
        assertThrows(IllegalArgumentException.class,
                () -> controller.writeNamedGame(source, "a".repeat(65)));
        assertArrayEquals(bytes, Files.readAllBytes(source.toPath()));
        clearInvocations(current);
        assertTrue(controller.loadModelFromFile(source));
        verify(current).setAndStartModel(loaded.capture());
        assertEquals(saved.getPlaythroughId(),
                loaded.getValue().getPlaythroughId());
    }
}
