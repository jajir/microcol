package org.microcol.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SavedGamesTest {
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
    void quickSavesAreScopedAndManualNamesNeverBecomePaths() throws Exception {
        final SavedGames saves = library();
        final String first = UUID.randomUUID().toString();
        final String second = UUID.randomUUID().toString();
        assertEquals(saves.target(first, true), saves.target(first, true));
        assertNotEquals(saves.target(first, true), saves.target(second, true));
        assertNotEquals(saves.target(first, false), saves.target(first, false));
        assertThrows(IllegalArgumentException.class,
                () -> saves.target("../../outside", true));
        assertTrue(saves.list().isEmpty());
    }

    @Test
    void listingClassifiesCorruptionWithoutChangingAnyFile() throws Exception {
        final SavedGames saves = library();
        final Path manual = directory.resolve("old.microcol");
        Files.writeString(manual, "broken JSON");
        final Path recovery = directory.resolve("autosave.microcol");
        Files.writeString(recovery, "{}");
        final Path quick = saves.target(UUID.randomUUID().toString(), true)
                .toPath();
        final GameModelPo snapshot = new GameModelPo();
        snapshot.setSaveName("Přístav / před bitvou");
        new GameModelPoDao().saveToFile(quick.toString(), snapshot);
        final byte[] bytes = Files.readAllBytes(quick);
        final var entries = saves.list();
        assertEquals(3, entries.size());
        assertEquals(SavedGames.Kind.MANUAL,
                entries.stream()
                        .filter(entry -> entry.file.toPath().equals(manual))
                        .findFirst().orElseThrow().kind);
        assertEquals(SavedGames.Kind.AUTOMATIC,
                entries.stream()
                        .filter(entry -> entry.file.toPath().equals(recovery))
                        .findFirst().orElseThrow().kind);
        final var entry = entries.stream()
                .filter(value -> value.file.toPath().equals(quick)).findFirst()
                .orElseThrow();
        assertEquals(SavedGames.Kind.QUICK, entry.kind);
        assertEquals("Přístav / před bitvou", entry.getName());
        assertArrayEquals(bytes, Files.readAllBytes(quick));
        assertEquals("broken JSON", Files.readString(manual));
    }
}
