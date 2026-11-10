package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SavedGamesTest {
    private static final String MAP = "/maps/default-campaign-mission-2-thrive.akropia";

    @TempDir
    Path directory;

    private SavedGames library() {
        return new SavedGames(new PersistingTool() {
            @Override
            public java.io.File getRootSaveDirectory() {
                return directory.toFile();
            }
        }, new GameModelPoDao());
    }

    @Test
    void unusedNamesRespectCollisionsUnicodeAndLength() throws Exception {
        final SavedGames saves = library();
        assertEquals("dutch-1600", saves.unusedName("dutch-1600"));
        Files.writeString(saves.target("Dutch-1600").toPath(), "existing");
        Files.writeString(saves.target("dutch-1600-2").toPath(), "existing");
        assertEquals("dutch-1600-3", saves.unusedName("dutch-1600"));
        final String longName = "é".repeat(64);
        Files.writeString(saves.target(longName).toPath(), "existing");
        assertEquals("é".repeat(62) + "-2", saves.unusedName(longName));
        assertEquals("existing",
                Files.readString(saves.target("Dutch-1600").toPath()));
        assertThrows(IllegalArgumentException.class,
                () -> saves.unusedName("autosave-000"));
    }

    @Test
    void sharedQuickSlotAndReadableManualNames() {
        final SavedGames saves = library();
        assertEquals(directory.resolve("saves/quick.akropia"),
                saves.quickFile().toPath());
        assertEquals(directory.resolve("saves/dutch-1600.akropia"),
                saves.target("dutch-1600").toPath());
        assertThrows(IllegalArgumentException.class,
                () -> saves.target("../../outside"));
        assertTrue(saves.list().isEmpty());
    }

    @Test
    void listingClassifiesCorruptionWithoutChangingAnyFile() throws Exception {
        final SavedGames saves = library();
        final Path manual = saves.target("old").toPath();
        Files.writeString(manual, "broken JSON");
        final Path recovery = directory.resolve("saves/autosave-000.akropia");
        Files.writeString(recovery, "{}");
        final Path quick = saves.quickFile().toPath();
        final GameModelPo snapshot = new GameModelPoDao()
                .loadFromClassPath(MAP);
        // The stored kind is manual; the catalogue location makes it quick.
        new GameModelPoDao().savePlayerGame(quick.toString(), snapshot,
                SaveArchives.metadata(snapshot, "Přístav / před bitvou"), null);
        final byte[] bytes = Files.readAllBytes(quick);
        final var entries = saves.list();
        assertEquals(3, entries.size());
        final var old = entries.stream()
                .filter(entry -> entry.file.toPath().equals(manual)).findFirst()
                .orElseThrow();
        assertEquals(SavedGames.Kind.MANUAL, old.kind);
        assertEquals(SaveArchiveException.Reason.INVALID, old.problem);
        // The technical extension is not part of the shown name.
        assertEquals("old", old.getName());
        assertEquals(SavedGames.Kind.AUTOMATIC,
                entries.stream()
                        .filter(entry -> entry.file.toPath().equals(recovery))
                        .findFirst().orElseThrow().kind);
        final var entry = entries.stream()
                .filter(value -> value.file.toPath().equals(quick)).findFirst()
                .orElseThrow();
        assertEquals(SavedGames.Kind.QUICK, entry.kind);
        assertNull(entry.problem);
        assertEquals("Přístav / před bitvou", entry.getName());
        assertArrayEquals(bytes, Files.readAllBytes(quick));
        assertEquals("broken JSON", Files.readString(manual));
    }

    @Test
    void listingReadsMetadataOnlyAndSortsByTheStoredTime() throws Exception {
        final SavedGames saves = library();
        final GameModelPoDao dao = new GameModelPoDao();
        final Path older = saves.target("older").toPath();
        final Path newer = saves.target("newer").toPath();
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final SaveSummaryPo summary = SaveArchives.metadata(data, "Newer")
                .getSummary();
        dao.savePlayerGame(newer.toString(), data,
                SaveMetadataPo.of("Newer", SavedGames.Kind.MANUAL,
                        Instant.parse("2026-11-01T12:00:00Z"),
                        data.getPlaythroughId(), summary),
                SaveArchives.pngHeader(16, 9));
        dao.savePlayerGame(older.toString(), data,
                SaveMetadataPo.of("Older", SavedGames.Kind.MANUAL,
                        Instant.parse("2026-10-01T12:00:00Z"),
                        data.getPlaythroughId(), summary),
                null);
        // A broken game entry is found only when the save is loaded.
        SaveArchives.replace(newer, "game.json", "{");
        final byte[] before = Files.readAllBytes(newer);

        final var entries = saves.list();

        assertEquals(java.util.List.of("Newer", "Older"),
                entries.stream().map(SavedGames.Entry::getName)
                        .collect(java.util.stream.Collectors.toList()));
        assertNull(entries.get(0).problem);
        assertEquals(Instant.parse("2026-11-01T12:00:00Z").toEpochMilli(),
                entries.get(0).modified);
        assertTrue(saves.preview(entries.get(0)).isPresent());
        assertFalse(saves.preview(entries.get(1)).isPresent());
        assertArrayEquals(before, Files.readAllBytes(newer));
    }

    @Test
    void unsupportedSavesAreListedWithTheirReason() throws Exception {
        final SavedGames saves = library();
        final Path file = saves.target("future").toPath();
        SaveArchives.playerSave(file,
                new GameModelPoDao().loadFromClassPath(MAP));
        final String metadata = new String(
                SaveArchives.read(file).get("metadata.json"),
                java.nio.charset.StandardCharsets.UTF_8);
        SaveArchives.replace(file, "metadata.json", metadata
                .replace("\"formatVersion\": 1", "\"formatVersion\": 9"));

        final var entry = saves.list().get(0);

        assertEquals(SaveArchiveException.Reason.UNSUPPORTED, entry.problem);
        assertNull(entry.metadata);
        assertFalse(saves.preview(entry).isPresent());
    }

    @Test
    void listingIgnoresParentAndNestedFilesWithoutChangingThem()
            throws Exception {
        final SavedGames saves = library();
        final Path visible = saves.target("Visible").toPath();
        Files.writeString(visible, "invalid but visible");
        final Path nested = Files
                .createDirectories(directory.resolve("saves/hashed-game"))
                .resolve("hidden.akropia");
        final Path parent = directory.resolve("legacy.akropia");
        Files.writeString(nested, "nested bytes");
        Files.writeString(parent, "parent bytes");
        Files.writeString(directory.resolve("saves/notes.txt"), "not a save");
        Files.createDirectory(directory.resolve("saves/directory.akropia"));

        assertEquals(java.util.List.of(visible),
                saves.list().stream().map(entry -> entry.file.toPath())
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals("nested bytes", Files.readString(nested));
        assertEquals("parent bytes", Files.readString(parent));
        assertEquals("invalid but visible", Files.readString(visible));
    }
}
