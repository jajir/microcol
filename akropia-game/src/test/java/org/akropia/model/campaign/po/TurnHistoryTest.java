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
        return games.saveTurnSnapshot(game, saves, null);
    }

    @Test
    void tenSharedSlotsWrapAndResumeAfterRestartAndReplay() throws Exception {
        final GameModel first = game();
        final GameModel second = game();
        assertTrue(saves.turnSnapshots().isEmpty());
        for (int sequence = 1; sequence <= 21; sequence++) {
            final TurnSnapshot saved = snapshot(
                    sequence % 2 == 0 ? second : first);
            assertEquals(
                    String.format("autosave-%03d.akropia", (sequence - 1) % 10),
                    saved.getFile().getName());
            assertEquals(directory.resolve("saves"),
                    saved.getFile().getParentFile().toPath());
            assertEquals(Math.min(sequence, 10), saves.turnSnapshots().size());
            assertEquals(sequence, new GameModelPoDao()
                    .loadMetadata(saved.getFile()).getAutosaveSequence());
            saves = new SavedGames(paths, new GameModelPoDao());
            assertEquals(sequence + 1, saves.nextSequence());
        }
        final GameModel replay = games
                .loadFromFile(saves.turnSnapshots().get(0), new EventBus());
        assertEquals("autosave-001.akropia",
                snapshot(replay).getFile().getName());
        for (final RecoverySlot slot : RecoverySlot.values()) {
            assertFalse(paths.getRecoveryFile(slot).exists());
        }
        assertTrue(saves.list().stream()
                .allMatch(entry -> entry.kind == SavedGames.Kind.AUTOMATIC));
    }

    @Test
    void failedReplacementKeepsEverySlotAndTheNextSequence() throws Exception {
        final GameModel game = game();
        for (int i = 0; i < 10; i++) {
            snapshot(game);
        }
        final java.util.Map<Path, byte[]> before = new java.util.HashMap<>();
        for (final File file : saves.turnSnapshots()) {
            before.put(file.toPath(), Files.readAllBytes(file.toPath()));
        }
        final GameModelDao failing = games(
                new GameModelPoDao((source, target) -> {
                    throw new IOException("Interrupted atomic replacement");
                }));
        assertThrows(AkropiaException.class,
                () -> failing.saveTurnSnapshot(game, saves, null));
        assertEquals(11, saves.nextSequence());
        for (final var entry : before.entrySet()) {
            assertArrayEquals(entry.getValue(),
                    Files.readAllBytes(entry.getKey()));
        }
        try (var files = Files.list(directory.resolve("saves"))) {
            assertEquals(10, files.count());
        }
        assertEquals("autosave-000.akropia",
                snapshot(game).getFile().getName());
    }

    @Test
    void clockRollbackDoesNotReorderTheBrowserOrReuseTheNewestSlot()
            throws Exception {
        final GameModel game = game();
        final File first = snapshot(game).getFile();
        final File second = snapshot(game).getFile();
        final var archive = SaveArchives.read(second.toPath());
        final var metadata = com.google.gson.JsonParser
                .parseString(new String(archive.get("metadata.json"),
                        java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
        metadata.addProperty("savedAt", "2000-01-01T00:00:00Z");
        SaveArchives.replace(second.toPath(), "metadata.json",
                metadata.toString());
        assertEquals(List.of(second, first), saves.list().stream()
                .map(entry -> entry.file).collect(Collectors.toList()));
        assertEquals(3, saves.nextSequence());
        assertEquals("autosave-002.akropia",
                snapshot(game).getFile().getName());
    }

    @Test
    void exhaustedMetadataSequenceReportsASaveFailureWithoutChangingTheArchive()
            throws Exception {
        final File file = snapshot(game()).getFile();
        final var metadata = com.google.gson.JsonParser
                .parseString(new String(
                        SaveArchives.read(file.toPath()).get("metadata.json"),
                        java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
        metadata.addProperty("autosaveSequence", Long.MAX_VALUE);
        SaveArchives.replace(file.toPath(), "metadata.json",
                metadata.toString());
        final byte[] before = Files.readAllBytes(file.toPath());
        assertThrows(AkropiaException.class, () -> snapshot(game()));
        assertArrayEquals(before, Files.readAllBytes(file.toPath()));
    }

    @Test
    void manualAndQuickFilesStayOutsideTheRing() throws Exception {
        final GameModel game = game();
        final File manual = saves.target("  Před útokem  ");
        assertEquals("Před útokem.akropia", manual.getName());
        games.saveNamedGame(manual, game, "Před útokem");
        games.saveGame(saves.quickFile(), game, SavedGames.Kind.QUICK,
                "Quick save", null);
        final byte[] named = Files.readAllBytes(manual.toPath());
        final byte[] quick = Files.readAllBytes(saves.quickFile().toPath());
        for (int i = 0; i < 21; i++) {
            snapshot(game);
        }
        assertArrayEquals(named, Files.readAllBytes(manual.toPath()));
        assertArrayEquals(quick,
                Files.readAllBytes(saves.quickFile().toPath()));
        assertEquals(manual, saves.target("PŘED ÚTOKEM.akropia"));
        assertEquals(manual, saves.target(java.text.Normalizer
                .normalize("Před útokem", java.text.Normalizer.Form.NFD)));
        assertEquals(12, saves.list().size());
    }

    @Test
    void invalidOrReservedManualNamesCannotEscapeOrEnterManagedSlots() {
        for (final String name : List.of("../escape", "a/b", "a\\b", "a:b",
                "CON", "NUL.txt", "end.", "autosave-000",
                "AUTOSAVE-009.akropia", "quick", "latest-turn",
                "previous-decade")) {
            assertThrows(IllegalArgumentException.class,
                    () -> saves.target(name), name);
        }
        assertEquals("Žluťoučký kůň",
                SavedGames.manualName("\u2003Žluťoučký kůň.akropia\u2003"));
        assertThrows(IllegalArgumentException.class,
                () -> saves.target("x".repeat(65)));
    }
}
