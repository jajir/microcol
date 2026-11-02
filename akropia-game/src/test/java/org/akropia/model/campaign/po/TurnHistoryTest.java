package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.AkropiaException;
import org.akropia.model.campaign.CampaignModule;
import org.akropia.model.campaign.GameModel;
import org.akropia.model.campaign.GameModelDao;
import org.akropia.model.campaign.store.CampaignsDao;
import org.akropia.model.campaign.store.CampaignsPo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;

/** Rolling turn snapshots of one playthrough (R-0388). */
class TurnHistoryTest {
    private static final String MAP = "/maps/default-campaign-mission-2-thrive.akropia";

    @TempDir
    Path directory;

    private PersistingTool paths;

    private SavedGames saves;

    private GameModelDao games;

    @BeforeEach
    void prepare() {
        paths = new PersistingTool() {
            @Override
            public File getRootSaveDirectory() {
                return directory.toFile();
            }
        };
        saves = new SavedGames(paths, new GameModelPoDao());
        games = games(new GameModelPoDao());
    }

    private static GameModelDao games(final GameModelPoDao files) {
        final CampaignsDao campaigns = mock(CampaignsDao.class);
        when(campaigns.load()).thenReturn(new CampaignsPo());
        return Guice.createInjector(new CampaignModule(), binder -> {
            binder.bind(CampaignsDao.class).toInstance(campaigns);
            binder.bind(GameModelPoDao.class).toInstance(files);
        }).getInstance(GameModelDao.class);
    }

    private GameModel game() {
        return games.loadFromClassPath(MAP, new EventBus());
    }

    private TurnSnapshot snapshot(final GameModel game) {
        return games.saveTurnSnapshot(game, saves, paths,
                List.of(RecoverySlot.LATEST_TURN), null);
    }

    private List<String> names(final String playthroughId) {
        return saves.turnSnapshots(playthroughId).stream().map(File::getName)
                .collect(Collectors.toList());
    }

    private static String name(final int sequence) {
        return String.format("turn-%010d.akropia", sequence);
    }

    @Test
    void historyKeepsTheFiveNewestWritesAndCopiesTheLatestTurn()
            throws Exception {
        final GameModel game = game();
        final String id = game.getPlaythroughId();
        assertEquals(List.of(), names(id));

        final TurnSnapshot first = snapshot(game);
        assertEquals(List.of(name(1)), names(id));
        assertTrue(first.isPruned());
        assertTrue(first.getSlotFailure().isEmpty());
        assertArrayEquals(Files.readAllBytes(first.getFile().toPath()),
                Files.readAllBytes(paths
                        .getRecoveryFile(RecoverySlot.LATEST_TURN).toPath()));
        assertEquals(SavedGames.Kind.AUTOMATIC,
                new GameModelPoDao().loadMetadata(first.getFile()).getKind());

        for (int i = 2; i <= 5; i++) {
            snapshot(game);
        }
        assertEquals(List.of(name(1), name(2), name(3), name(4), name(5)),
                names(id));

        // The sixth successful write removes only the oldest one.
        snapshot(game);
        assertEquals(List.of(name(2), name(3), name(4), name(5), name(6)),
                names(id));
    }

    @Test
    void sequenceSurvivesRestartAndReplayAppendsAfterTheNewest()
            throws Exception {
        final GameModel game = game();
        final String id = game.getPlaythroughId();
        for (int i = 1; i <= 5; i++) {
            snapshot(game);
        }
        // A later session continues the sequence from the files, not from
        // the game date or the clock.
        saves = new SavedGames(paths, new GameModelPoDao());
        final File oldest = saves.turnSnapshots(id).get(0);
        final GameModel replayed = games.loadFromFile(oldest, new EventBus());
        assertEquals(id, replayed.getPlaythroughId());

        final TurnSnapshot appended = snapshot(replayed);

        assertEquals(name(6), appended.getFile().getName());
        assertEquals(List.of(name(2), name(3), name(4), name(5), name(6)),
                names(id));
        assertEquals(games.gameplayState(replayed), appended.getState());
    }

    @Test
    void otherCategoriesAndPlaythroughsStayOutsideTheRotation()
            throws Exception {
        final GameModel game = game();
        final String id = game.getPlaythroughId();
        final Path own = directory.resolve("saves").resolve(id);
        Files.createDirectories(own);
        final Path other = directory.resolve("saves")
                .resolve(UUID.randomUUID().toString());
        Files.createDirectories(other);
        final Path quick = own.resolve("quick.akropia");
        final Path manual = own.resolve(UUID.randomUUID() + ".akropia");
        final Path similar = own.resolve("turn-1.akropia");
        final Path imported = directory.resolve(name(1));
        for (final Path kept : List.of(quick, manual, similar, imported)) {
            Files.writeString(kept, "kept");
        }
        for (int i = 1; i <= 7; i++) {
            Files.writeString(other.resolve(name(i)), "other playthrough");
        }
        Files.writeString(
                paths.getRecoveryFile(RecoverySlot.PREVIOUS_DECADE).toPath(),
                "decade");
        Files.writeString(
                paths.getRecoveryFile(RecoverySlot.LATEST_POSITION).toPath(),
                "latest position");

        for (int i = 1; i <= 6; i++) {
            snapshot(game);
        }

        assertEquals(5, names(id).size());
        for (final Path kept : List.of(quick, manual, similar, imported)) {
            assertEquals("kept", Files.readString(kept));
        }
        try (var files = Files.list(other)) {
            assertEquals(7, files.count());
        }
        assertEquals("decade", Files.readString(
                paths.getRecoveryFile(RecoverySlot.PREVIOUS_DECADE).toPath()));
        assertEquals("latest position", Files.readString(
                paths.getRecoveryFile(RecoverySlot.LATEST_POSITION).toPath()));
        // Only the managed name in a playthrough folder is a recovery point.
        final var kinds = saves.list().stream().collect(Collectors
                .toMap(entry -> entry.file.toPath(), entry -> entry.kind));
        assertEquals(SavedGames.Kind.MANUAL, kinds.get(imported));
        assertEquals(SavedGames.Kind.MANUAL, kinds.get(similar));
        assertEquals(SavedGames.Kind.AUTOMATIC,
                kinds.get(own.resolve(name(6))));
    }

    @Test
    void failedWriteKeepsTheHistoryAndExposesNoPartialSave() throws Exception {
        final GameModel game = game();
        final String id = game.getPlaythroughId();
        for (int i = 1; i <= 5; i++) {
            snapshot(game);
        }
        final byte[] latest = Files.readAllBytes(
                paths.getRecoveryFile(RecoverySlot.LATEST_TURN).toPath());
        final GameModelDao failing = games(
                new GameModelPoDao((source, target) -> {
                    throw new IOException("Interrupted write");
                }));

        assertThrows(AkropiaException.class,
                () -> failing.saveTurnSnapshot(game, saves, paths,
                        List.of(RecoverySlot.LATEST_TURN), null));

        assertEquals(List.of(name(1), name(2), name(3), name(4), name(5)),
                names(id));
        assertArrayEquals(latest, Files.readAllBytes(
                paths.getRecoveryFile(RecoverySlot.LATEST_TURN).toPath()));
        try (var files = Files.list(directory.resolve("saves").resolve(id))) {
            assertEquals(5, files.count(), "No temporary file remains");
        }
    }

    @Test
    void failedCleanupKeepsExtraSnapshotsUntilALaterAutosave()
            throws Exception {
        final GameModel game = game();
        final String id = game.getPlaythroughId();
        saves = new SavedGames(paths, new GameModelPoDao(), file -> {
            throw new IOException("Locked");
        });
        for (int i = 1; i <= 5; i++) {
            assertTrue(snapshot(game).isPruned());
        }

        final TurnSnapshot sixth = snapshot(game);

        assertFalse(sixth.isPruned());
        assertEquals(6, names(id).size());
        assertTrue(sixth.getFile().isFile());

        // The retry of a later successful autosave removes the extras.
        saves = new SavedGames(paths, new GameModelPoDao());
        assertTrue(snapshot(game).isPruned());
        assertEquals(List.of(name(3), name(4), name(5), name(6), name(7)),
                names(id));
    }

    @Test
    void failedSlotCopyStillKeepsTheCompleteSnapshot() throws Exception {
        final GameModel game = game();
        // A folder where the recovery slot belongs cannot be replaced.
        final Path slot = paths.getRecoveryFile(RecoverySlot.LATEST_TURN)
                .toPath();
        Files.createDirectories(slot);
        Files.writeString(slot.resolve("content"), "kept");

        final TurnSnapshot snapshot = snapshot(game);

        assertTrue(snapshot.getSlotFailure().isPresent());
        assertEquals(List.of(name(1)), names(game.getPlaythroughId()));
        assertEquals(game.getPlaythroughId(), new GameModelPoDao()
                .loadPlayerGame(snapshot.getFile()).getPlaythroughId());
    }

    @Test
    void nextSnapshotNeverNamesAnExistingFile() throws Exception {
        final String id = UUID.randomUUID().toString();
        final File next = saves.nextTurnSnapshot(id);
        assertEquals(name(1), next.getName());
        assertFalse(next.exists());
        Files.writeString(next.toPath(), "written");
        assertEquals(name(2), saves.nextTurnSnapshot(id).getName());
        assertThrows(IllegalArgumentException.class,
                () -> saves.nextTurnSnapshot("../outside"));
    }
}
