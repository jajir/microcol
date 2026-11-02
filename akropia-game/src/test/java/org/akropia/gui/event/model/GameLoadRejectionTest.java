package org.akropia.gui.event.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.gui.FileSelectingService;
import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.i18n.I18n;
import org.akropia.model.Model;
import org.akropia.model.campaign.po.PersistingTool;
import org.akropia.model.campaign.po.SavedGames;
import org.akropia.model.campaign.CampaignManager;
import org.akropia.model.campaign.CampaignModule;
import org.akropia.model.campaign.GameModelDao;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.campaign.po.SaveArchives;
import org.akropia.model.campaign.store.CampaignsDao;
import org.akropia.model.campaign.store.CampaignsPo;
import org.akropia.model.store.PlaceMapPo;

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
    void prepare() throws Exception {
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
                "/maps/default-campaign-mission-2-thrive.akropia");
        source = directory.resolve("game.akropia").toFile();
        save(THRIVE);
    }

    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.akropia";

    private static final String FREE_PLAY = "/maps/free-play.akropia";

    /**
     * Write the current data as a player save. Its metadata describe the valid
     * authored state with the identity and campaign keys of the data, so only
     * the game data can be wrong.
     */
    private void save(final String map) throws Exception {
        files.savePlayerGame(source.getAbsolutePath(), data, SaveArchives
                .metadata(files.loadFromClassPath(map), "Thrive", data), null);
    }

    private PersistingService persistingService() {
        final PersistingTool paths = mock(PersistingTool.class);
        // Completed/inactive games must not need a dialog host to load another.
        return new PersistingService(controller, new EventBus(), paths,
                mock(FileSelectingService.class), current, diagnostics,
                new SavedGames(paths, files), null, mock(I18n.class),
                mock(GamePreferences.class));
    }

    @Test
    void leavingWithoutAnActiveGameNeedsNoSaveConfirmation() {
        assertTrue(persistingService().confirmLeave());
        verify(current, never()).getModel();
    }

    @Test
    void leavingAFinishedGameNeedsNoSaveConfirmation() {
        final Model model = mock(Model.class);
        when(current.isGameModelReady()).thenReturn(true);
        when(current.getModel()).thenReturn(model);
        when(model.isGameFinished()).thenReturn(true);

        assertTrue(persistingService().confirmLeave());
    }

    @Test
    void rejectedDataCannotReplaceTheCurrentGameOrRewriteTheFile()
            throws Exception {
        data.getModel().getColonies().get(1)
                .setName(data.getModel().getColonies().get(0).getName());
        save(THRIVE);
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
                directory.resolve("missing.akropia").toFile()));
        assertFalse(controller.loadModelFromFile(directory.toFile()));
        verify(current, times(2)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void missingIdentityAndInvalidReferencesDoNotReplaceTheGame()
            throws Exception {
        data.getModel().getGameManager().getPlayers().get(0).setName(null);
        save(THRIVE);
        assertRejectedSource();

        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        data.getModel().getUnits().get(0).setOwnerId("unknown participant");
        save(THRIVE);
        assertRejectedSource();

        verify(current, times(2)).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void missingNestedLocationRejectsBeforeReplacingTheCurrentGame()
            throws Exception {
        data.getModel().getUnits().get(0).setPlaceMap(new PlaceMapPo());
        save(THRIVE);

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void nullShipCargoSlotRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        data = files.loadFromClassPath("/maps/free-play.akropia");
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getType().isShip()
                        && !unit.getCargo().getSlots().isEmpty())
                .findFirst().orElseThrow().getCargo().getSlots().set(0, null);
        save(FREE_PLAY);

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void truncatedTerrainRowsRejectWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(files.loadGameJson(source)).getAsJsonObject();
        final com.google.gson.JsonArray rows = root.getAsJsonObject("model")
                .getAsJsonObject("map").getAsJsonArray("tiles");
        rows.remove(rows.size() - 1);
        SaveArchives.replace(source.toPath(), "game.json", root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void nullTerrainRowRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(files.loadGameJson(source)).getAsJsonObject();
        root.getAsJsonObject("model").getAsJsonObject("map")
                .getAsJsonArray("tiles")
                .set(1, com.google.gson.JsonNull.INSTANCE);
        SaveArchives.replace(source.toPath(), "game.json", root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void shortTerrainRowRejectsWithoutReplacingTheCurrentGame()
            throws Exception {
        final com.google.gson.JsonObject root = new com.google.gson.JsonParser()
                .parse(files.loadGameJson(source)).getAsJsonObject();
        root.getAsJsonObject("model").getAsJsonObject("map")
                .getAsJsonArray("tiles")
                .set(1, new com.google.gson.JsonPrimitive("row-0001:_"));
        SaveArchives.replace(source.toPath(), "game.json", root.toString());

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void unknownCampaignAndMissionDoNotReplaceTheGame() throws Exception {
        data.setCampaignName("unknown campaign");
        save(THRIVE);
        assertRejectedSource();

        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        data.setMissionName("unknown mission");
        save(THRIVE);
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
                .forClass(org.akropia.model.campaign.GameModel.class);
        verify(current).setAndStartModel(loaded.capture());
        when(current.getGameModel()).thenReturn(loaded.getValue());
        controller.writeNamedGame(source, "\u2003Přístav / první\u00a0");
        final var saved = files.loadPlayerGame(source);
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

    @Test
    void savesStoreKindSummaryAndPreviewOfTheSameState() throws Exception {
        assertTrue(controller.loadModelFromFile(source));
        final var loaded = org.mockito.ArgumentCaptor
                .forClass(org.akropia.model.campaign.GameModel.class);
        verify(current).setAndStartModel(loaded.capture());
        when(current.getGameModel()).thenReturn(loaded.getValue());
        final Model model = loaded.getValue().getModel();
        final File quick = directory.resolve("quick.akropia").toFile();

        controller.writeNamedGame(quick, "Quick save", SavedGames.Kind.QUICK);
        controller.writeModelToFile(source, SavedGames.Kind.AUTOMATIC);

        final var metadata = files.loadMetadata(quick);
        assertEquals(SavedGames.Kind.QUICK, metadata.getKind());
        assertEquals("Quick save", metadata.getSaveName());
        assertEquals(loaded.getValue().getPlaythroughId(),
                metadata.getPlaythroughId());
        assertEquals(model.getCalendar().getCurrentYear(),
                metadata.getSummary().getYear());
        // The summary and the game data describe the same snapshot.
        final var game = files.loadPlayerGame(quick);
        assertEquals(game.getCampaignName(),
                metadata.getSummary().getCampaignName());
        assertEquals(game.getMissionName(),
                metadata.getSummary().getMissionName());
        assertEquals(game.getModel().getCalendar().getNumberOfPlayedTurns(),
                model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(files.loadPreview(quick).isPresent(),
                "A player save has its map preview");
        assertEquals(SavedGames.Kind.AUTOMATIC,
                files.loadMetadata(source).getKind());
        // Without a manual name the save names the player and the year.
        assertTrue(files.loadMetadata(source).getSaveName().contains(
                String.valueOf(model.getCalendar().getCurrentYear())));
        assertTrue(files.loadPreview(source).isPresent());
    }

    @Test
    void plainJsonIsNotAPlayerSaveAndDoesNotReplaceTheGame() throws Exception {
        files.saveScenarioToFile(source.getAbsolutePath(), data);

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }

    @Test
    void unsupportedArchiveDoesNotReplaceTheGame() throws Exception {
        final String metadata = new String(
                SaveArchives.read(source.toPath()).get("metadata.json"),
                java.nio.charset.StandardCharsets.UTF_8);
        SaveArchives.replace(source.toPath(), "metadata.json", metadata
                .replace("\"formatVersion\": 1", "\"formatVersion\": 7"));

        assertRejectedSource();
        verify(current).showInvalidSavedGame();
        verify(current, never()).setAndStartModel(any());
    }
}
