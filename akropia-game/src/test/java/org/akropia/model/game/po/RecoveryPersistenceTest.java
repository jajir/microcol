package org.akropia.model.game.po;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.AkropiaException;

class RecoveryPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void failedAtomicReplacementKeepsPriorBytesAndRemovesTemporaryFile()
            throws Exception {
        final Path destination = directory.resolve("saved.akropia");
        Files.writeString(destination, "previous valid bytes");
        final GameModelPoDao writer = new GameModelPoDao((source, target) -> {
            throw new IOException("Atomic replacement unavailable");
        });

        assertThrows(AkropiaException.class, () -> writer
                .saveToFile(destination.toString(), new GameModelPo()));
        assertEquals("previous valid bytes", Files.readString(destination));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void failedArchiveReplacementKeepsThePreviousValidSave() throws Exception {
        final Path destination = directory.resolve("saved.akropia");
        final GameModelPo first = new GameModelPo();
        first.setSaveName("first");
        // Valid player saves of a real model; only the campaign keys differ.
        final GameModelPo valid = new GameModelPoDao()
                .loadFromClassPath("/maps/fixture-developed-world.akropia");
        first.setModel(valid.getModel());
        SaveArchives.playerSave(destination, first);
        final byte[] previous = Files.readAllBytes(destination);
        final GameModelPo second = new GameModelPo();
        second.setSaveName("second");
        second.setModel(valid.getModel());
        final SaveMetadataPo metadata = SaveArchives.metadata(second, "second");
        final GameModelPoDao writer = new GameModelPoDao((source, target) -> {
            throw new IOException("Atomic replacement unavailable");
        });

        assertThrows(AkropiaException.class,
                () -> writer.savePlayerGame(destination.toString(), second,
                        metadata, SaveArchives.pngHeader(10, 10)));

        assertArrayEquals(previous, Files.readAllBytes(destination));
        assertEquals("first", new GameModelPoDao()
                .loadPlayerGame(destination.toFile()).getSaveName());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void successfulReplacementUsesTheSharedWriter() throws Exception {
        final Path destination = directory.resolve("saved.akropia");
        final GameModelPo first = new GameModelPo();
        first.setSaveName("first");
        final GameModelPo second = new GameModelPo();
        second.setSaveName("second");
        final GameModelPoDao writer = new GameModelPoDao();

        writer.saveToFile(destination.toString(), first);
        writer.saveToFile(destination.toString(), second);

        assertEquals("second",
                writer.loadFromFile(destination.toFile()).getSaveName());
    }
}
