package org.microcol.gui.event.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.i18n.I18n;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignModule;
import org.microcol.model.campaign.GameModel;
import org.microcol.model.campaign.GameModelDao;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.model.campaign.po.SavedGames;
import org.microcol.model.campaign.store.CampaignsDao;
import org.microcol.model.campaign.store.CampaignsPo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The shared leave rule knows when the live game is safely saved, and turn
 * snapshots establish that state only after a complete write (R-0387, R-0388).
 */
class SaveStatePolicyTest {
    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    @TempDir
    Path directory;

    private final GameModelController current = mock(GameModelController.class);

    private final GamePreferences preferences = mock(GamePreferences.class);

    private final FileSelectingService files = mock(FileSelectingService.class);

    private GameModelDao games;

    private PersistingTool paths;

    private SavedGames saves;

    private PersistingService service;

    private GameModel game;

    private boolean ready = true;

    private String continueFile;

    @BeforeEach
    void prepare() throws Exception {
        final CampaignsDao campaigns = mock(CampaignsDao.class);
        when(campaigns.load()).thenReturn(new CampaignsPo());
        final Injector injector = Guice.createInjector(new CampaignModule(),
                binder -> binder.bind(CampaignsDao.class)
                        .toInstance(campaigns));
        games = injector.getInstance(GameModelDao.class);
        final DiagnosticLog diagnostics = new DiagnosticLog(
                directory.resolve("logs"), "test-build");
        final GameController controller = new GameController(current, games,
                injector.getInstance(CampaignManager.class), new EventBus(),
                diagnostics);
        final File root = Files.createDirectories(directory.resolve("home"))
                .toFile();
        paths = new PersistingTool() {
            @Override
            public File getRootSaveDirectory() {
                return root;
            }
        };
        saves = new SavedGames(paths, new GameModelPoDao());
        game = games.loadFromClassPath(THRIVE, new EventBus());
        when(current.getGameModel()).thenAnswer(invocation -> game);
        when(current.getModel()).thenAnswer(invocation -> game.getModel());
        when(current.isGameModelReady()).thenAnswer(invocation -> ready);
        when(current.isHumanTurnReady()).thenAnswer(invocation -> ready);
        doAnswer(invocation -> {
            game = invocation.getArgument(0);
            return null;
        }).when(current).setAndStartModel(any());
        doAnswer(invocation -> {
            continueFile = invocation.getArgument(0);
            return null;
        }).when(preferences).setGameInProgressSaveFile(anyString());
        when(preferences.getGameInProgressSaveFile())
                .thenAnswer(invocation -> Optional.ofNullable(continueFile));
        when(preferences.isAutosaveEnabled()).thenReturn(true);
        service = new PersistingService(controller, new EventBus(), paths,
                files, current, diagnostics, saves, null, mock(I18n.class),
                preferences, Runnable::run);
    }

    private void checkpoint() {
        service.checkpoint(game.getModel(), List.of(RecoverySlot.LATEST_TURN));
    }

    private List<File> snapshots() {
        return saves.turnSnapshots(game.getPlaythroughId());
    }

    private Player human() {
        return game.getModel().getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow();
    }

    @Test
    void turnSnapshotSavesTheGameOnceAndBecomesContinue() {
        assertFalse(service.isProgressSaved());

        checkpoint();

        assertEquals(1, snapshots().size());
        assertEquals(snapshots().get(0).getAbsolutePath(), continueFile);
        assertTrue(service.isProgressSaved());
        // A repeated notice of the same boundary writes nothing new.
        checkpoint();
        assertEquals(1, snapshots().size());
    }

    @Test
    void viewChangesAndRejectedActionsKeepTheGameSaved() {
        checkpoint();

        game.getModel().setFocusedField(Location.of(3, 4));
        assertTrue(service.isProgressSaved());
        assertThrows(IllegalArgumentException.class,
                () -> human().setDisplayName(""));
        assertTrue(service.isProgressSaved());

        // A successful change without movement is unsaved progress.
        human().setDisplayName("Governor");
        assertFalse(service.isProgressSaved());
    }

    @Test
    void removedSnapshotNoLongerProtectsTheGame() throws Exception {
        checkpoint();

        Files.delete(snapshots().get(0).toPath());

        assertFalse(service.isProgressSaved());
    }

    @Test
    void incompleteTurnIsNeverSafelySaved() {
        checkpoint();
        ready = false;

        assertFalse(service.isProgressSaved());
    }

    @Test
    void newGameIsSavedOnlyByASuccessfulInitialSnapshot() {
        when(preferences.isAutosaveEnabled()).thenReturn(false);
        service.checkpointNewGame();
        assertEquals(List.of(), snapshots());
        assertFalse(service.isProgressSaved());

        when(preferences.isAutosaveEnabled()).thenReturn(true);
        service.checkpointNewGame();
        assertEquals(1, snapshots().size());
        assertTrue(service.isProgressSaved());
    }

    @Test
    void failedAutosaveKeepsContinueAndTheUnsavedStatus() throws Exception {
        final File loaded = directory.resolve("loaded.microcol").toFile();
        games.saveGame(loaded, game, SavedGames.Kind.MANUAL, null, null);
        continueFile = loaded.getAbsolutePath();
        ready = false;
        service.continueGame();
        ready = true;
        assertTrue(service.isProgressSaved());
        human().setDisplayName("Governor");
        Files.writeString(
                paths.getRootSaveDirectory().toPath().resolve("saves"),
                "not a folder");

        checkpoint();

        verify(current).showRecoverySaveFailure();
        assertEquals(loaded.getAbsolutePath(), continueFile);
        assertFalse(paths.getRecoveryFile(RecoverySlot.LATEST_TURN).exists());
        assertFalse(service.isProgressSaved());
    }

    @Test
    void loadedGameIsSavedWithoutRewritingItsFile() throws Exception {
        final File loaded = directory.resolve("loaded.microcol").toFile();
        games.saveGame(loaded, game, SavedGames.Kind.MANUAL, null, null);
        final byte[] bytes = Files.readAllBytes(loaded.toPath());
        final GameModel before = game;
        continueFile = loaded.getAbsolutePath();
        ready = false;

        service.continueGame();
        ready = true;

        assertNotSame(before, game);
        assertTrue(service.isProgressSaved());
        assertArrayEquals(bytes, Files.readAllBytes(loaded.toPath()));
        assertEquals(List.of(), snapshots());
        human().setDisplayName("Governor");
        assertFalse(service.isProgressSaved());
    }

    @Test
    void exportReportsSuccessOnlyForAWrittenFile() throws Exception {
        assertFalse(service.saveModel(game.getModel()),
                "A closed file chooser is no save");
        assertFalse(service.isProgressSaved());

        final File exported = directory.resolve("exported.microcol").toFile();
        when(files.saveFile(any(File.class), anyString())).thenReturn(exported);

        assertTrue(service.saveModel(game.getModel()));
        assertTrue(service.isProgressSaved());
        assertFalse(paths.getAutoSaveFile().exists(),
                "No recovery file replaces the chosen destination");
    }

    @Test
    void failedLoadKeepsTheGameAndItsSavedState() throws Exception {
        checkpoint();
        final GameModel before = game;
        final Path broken = directory.resolve("broken.microcol");
        Files.writeString(broken, "{");
        continueFile = broken.toString();

        service.continueGame();

        verify(current).showInvalidSavedGame();
        assertSame(before, game);
        assertTrue(service.isProgressSaved());
        assertEquals("{", Files.readString(broken));
    }
}
