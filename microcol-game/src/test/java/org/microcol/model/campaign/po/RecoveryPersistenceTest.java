package org.microcol.model.campaign.po;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.MicroColException;

class RecoveryPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void failedAtomicReplacementKeepsPriorBytesAndRemovesTemporaryFile()
            throws Exception {
        final Path destination = directory.resolve("saved.microcol");
        Files.writeString(destination, "previous valid bytes");
        final GameModelPoDao writer = new GameModelPoDao((source, target) -> {
            throw new IOException("Atomic replacement unavailable");
        });

        assertThrows(MicroColException.class, () -> writer
                .saveToFile(destination.toString(), new GameModelPo()));
        assertEquals("previous valid bytes", Files.readString(destination));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void successfulReplacementUsesTheSharedWriter() throws Exception {
        final Path destination = directory.resolve("saved.microcol");
        final GameModelPo first = new GameModelPo();
        first.setCampaignName("first");
        final GameModelPo second = new GameModelPo();
        second.setCampaignName("second");
        final GameModelPoDao writer = new GameModelPoDao();

        writer.saveToFile(destination.toString(), first);
        writer.saveToFile(destination.toString(), second);

        assertEquals("second",
                writer.loadFromFile(destination.toFile()).getCampaignName());
    }
}
