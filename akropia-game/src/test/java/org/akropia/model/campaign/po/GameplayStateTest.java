package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.Location;
import org.akropia.model.ModelListenerAdapter;
import org.akropia.model.Player;
import org.akropia.model.TurnPreparationException;
import org.akropia.model.campaign.CampaignModule;
import org.akropia.model.campaign.GameModel;
import org.akropia.model.campaign.GameModelDao;
import org.akropia.model.campaign.store.CampaignsDao;
import org.akropia.model.campaign.store.CampaignsPo;
import org.akropia.model.event.RoundStartedEvent;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.inject.Guice;

/**
 * The saved state is compared as production JSON of the gameplay; only the save
 * name and the focused square are not gameplay (R-0387).
 */
class GameplayStateTest {
    private static final String MAP = "/maps/default-campaign-mission-2-thrive.akropia";

    @TempDir
    Path directory;

    private GameModelDao games;

    @BeforeEach
    void prepare() {
        final CampaignsDao campaigns = mock(CampaignsDao.class);
        when(campaigns.load()).thenReturn(new CampaignsPo());
        games = Guice
                .createInjector(new CampaignModule(),
                        binder -> binder.bind(CampaignsDao.class)
                                .toInstance(campaigns))
                .getInstance(GameModelDao.class);
    }

    private GameModel game() {
        return games.loadFromClassPath(MAP, new EventBus());
    }

    private File file(final String name) {
        return directory.resolve(name).toFile();
    }

    private Player human(final GameModel game) {
        return game.getModel().getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow();
    }

    @Test
    void stateIsDeterministicAndIgnoresSaveNameAndFocusedSquare() {
        final GameModel game = game();
        final JsonElement first = games.gameplayState(game);

        assertEquals(first, games.gameplayState(game));
        game.getModel().setFocusedField(Location.of(2, 3));
        assertEquals(first, games.gameplayState(game));
        assertEquals(first, games.saveGame(file("a.akropia"), game,
                SavedGames.Kind.MANUAL, "First name", null));
        assertEquals(first, games.saveGame(file("b.akropia"), game,
                SavedGames.Kind.QUICK, "Other name", null));
    }

    @Test
    void loadedGameHasTheStateItsFileHolds() {
        final GameModel game = game();
        final File saved = file("saved.akropia");
        final JsonElement state = games.saveGame(saved, game,
                SavedGames.Kind.MANUAL, null, null);

        final GameModel loaded = games.loadFromFile(saved, new EventBus());

        assertEquals(state, games.gameplayState(loaded));
        assertTrue(SaveBaseline.of(saved, state)
                .covers(games.gameplayState(loaded)));
    }

    @Test
    void successfulMutationWithoutMovementNeedsSavingAndRejectedOneDoesNot() {
        final GameModel game = game();
        final File saved = file("saved.akropia");
        final SaveBaseline baseline = SaveBaseline.of(saved, games
                .saveGame(saved, game, SavedGames.Kind.MANUAL, null, null));

        // A rejected change leaves the game saved.
        assertThrows(IllegalArgumentException.class,
                () -> human(game).setDisplayName("a".repeat(65)));
        assertTrue(baseline.covers(games.gameplayState(game)));

        // A successful change that moves no unit is unsaved progress.
        human(game).setDisplayName("Governor");
        assertFalse(baseline.covers(games.gameplayState(game)));

        // Saving it again makes it the new baseline.
        final SaveBaseline again = SaveBaseline.of(saved, games.saveGame(saved,
                game, SavedGames.Kind.MANUAL, null, null));
        assertTrue(again.covers(games.gameplayState(game)));
    }

    @Test
    void missingOrReplacedBackingFileNoLongerProtectsTheGame()
            throws Exception {
        final GameModel game = game();
        final File saved = file("saved.akropia");
        final JsonElement state = games.saveGame(saved, game,
                SavedGames.Kind.MANUAL, null, null);
        final SaveBaseline baseline = SaveBaseline.of(saved, state);
        final byte[] bytes = Files.readAllBytes(saved.toPath());
        assertTrue(baseline.covers(state));

        Files.delete(saved.toPath());
        assertFalse(baseline.isFileAvailable());
        assertFalse(baseline.covers(state));

        // Another file at the same place is not the saved one.
        Files.write(saved.toPath(), new byte[] { 1, 2, 3 });
        assertFalse(baseline.covers(state));
        Files.write(saved.toPath(), bytes);
        assertTrue(saved.setLastModified(saved.lastModified() + 5000));
        assertFalse(baseline.covers(state));

        assertThrows(IllegalArgumentException.class,
                () -> SaveBaseline.of(file("missing.akropia"), state));
    }

    @Test
    void incompleteTurnHasNoSavedStateAndWritesNoSnapshot() {
        final GameModel game = game();
        final PersistingTool paths = new PersistingTool() {
            @Override
            public File getRootSaveDirectory() {
                return directory.toFile();
            }
        };
        final SavedGames saves = new SavedGames(paths, new GameModelPoDao());
        final boolean[] checked = { false };
        game.addListener(new ModelListenerAdapter() {
            @Override
            public void onRoundStarted(final RoundStartedEvent event) {
                assertThrows(TurnPreparationException.class,
                        () -> games.gameplayState(game));
                assertThrows(TurnPreparationException.class,
                        () -> games.saveTurnSnapshot(game, saves, paths,
                                List.of(RecoverySlot.LATEST_TURN), null));
                checked[0] = true;
            }
        });

        game.startGame();

        assertTrue(checked[0]);
        assertEquals(List.of(), saves.turnSnapshots(game.getPlaythroughId()));
        assertFalse(paths.getRecoveryFile(RecoverySlot.LATEST_TURN).exists());
        game.stop();
    }
}
