package org.microcol.model.campaign.po;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.BaseScore;
import org.microcol.model.Difficulty;
import org.microcol.model.HallOfFame;
import org.microcol.model.HallOfFameEntry;
import org.microcol.model.MicroColException;
import org.microcol.model.store.HallOfFameEntryPo;

/**
 * Storage of the persistent top ten (R-0197). A missing file is an empty
 * ranking; an invalid file is reported and kept unchanged; a failed write keeps
 * the previous file and leaves no temporary file behind.
 */
class HallOfFameDaoTest {

    @TempDir
    Path directory;

    private File file() {
        return directory.resolve("hall-of-fame.json").toFile();
    }

    private PersistingTool tool() {
        return new PersistingTool() {
            @Override
            public File getHallOfFameFile() {
                return file();
            }
        };
    }

    @Test
    void missingFileIsAnEmptyRanking() {
        assertTrue(new HallOfFameDao(tool()).load().getEntries().isEmpty());
    }

    @Test
    void savedRankingIsReadBackInStoredOrderAfterRestart() throws IOException {
        final HallOfFame hall = HallOfFame.empty().with(entry("first", 7))
                .with(entry("second", 7)).with(entry("top", 9));
        new HallOfFameDao(tool()).save(hall);
        final HallOfFame restarted = new HallOfFameDao(tool()).load();
        assertEquals(List.of("top", "first", "second"), identities(restarted));
        assertEquals(9L, restarted.getEntries().get(0).getTotal());
        assertEquals(Difficulty.CONQUISTADOR,
                restarted.getEntries().get(0).getDifficulty());
        assertEquals("Player top",
                restarted.getEntries().get(0).getPlayerDisplayName());
        assertTrue(Files.readString(file().toPath()).contains("\"top\""));
    }

    @Test
    void unreadableFileIsRejectedAndNotReplaced() throws IOException {
        Files.writeString(file().toPath(), "{ not json");
        assertThrows(MicroColException.class,
                () -> new HallOfFameDao(tool()).load());
        assertEquals("{ not json", Files.readString(file().toPath()));
    }

    @Test
    void emptyFileIsRejectedAndNotReplaced() throws IOException {
        Files.writeString(file().toPath(), "");
        assertThrows(MicroColException.class,
                () -> new HallOfFameDao(tool()).load());
        assertEquals("", Files.readString(file().toPath()));
    }

    @Test
    void fileWithoutEntriesIsRejected() throws IOException {
        Files.writeString(file().toPath(), "{}");
        assertThrows(MicroColException.class,
                () -> new HallOfFameDao(tool()).load());
    }

    @Test
    void invalidStoredRankingIsRejectedWithItsReason() throws IOException {
        // Two entries in the wrong order: the stored ranking is never
        // re-sorted.
        Files.writeString(file().toPath(), "{\"entries\":["
                + storedJson("low", 3) + "," + storedJson("high", 9) + "]}");
        assertThrows(MicroColException.class,
                () -> new HallOfFameDao(tool()).load());
        assertTrue(Files.readString(file().toPath()).contains("\"low\""));
    }

    @Test
    void failedWriteKeepsThePreviousFileAndLeavesNoTemporaryFile()
            throws IOException {
        new HallOfFameDao(tool())
                .save(HallOfFame.empty().with(entry("kept", 4)));
        final String before = Files.readString(file().toPath());
        final HallOfFameDao failing = new HallOfFameDao(tool(),
                (source, target) -> {
                    throw new IOException("disk full");
                });
        final HallOfFame bigger = HallOfFame.empty().with(entry("lost", 9));
        assertThrows(MicroColException.class, () -> failing.save(bigger));
        assertEquals(before, Files.readString(file().toPath()));
        try (Stream<Path> names = Files.list(directory)) {
            assertEquals(1, names.count());
        }
    }

    @Test
    void recordWithoutFrozenResultWritesNothing() {
        final HallOfFameDao dao = new HallOfFameDao(tool());
        assertTrue(dao.record(Optional.empty()).getEntries().isEmpty());
        assertFalse(file().exists());
    }

    @Test
    void recordedResultIsSavedOnceAndReadBackUnchanged() {
        final HallOfFameDao dao = new HallOfFameDao(tool());
        dao.record(Optional.of(entry("first", 4)));
        final HallOfFame again = dao.record(Optional.of(entry("first", 4)));
        assertEquals(List.of("first"), identities(again));
        assertEquals(List.of("first"), identities(dao.load()));
    }

    private static List<String> identities(final HallOfFame hall) {
        return hall.getEntries().stream().map(HallOfFameEntry::getIdentity)
                .collect(Collectors.toList());
    }

    private static HallOfFameEntry entry(final String identity,
            final int servants) {
        return HallOfFameEntry.load(po(identity, servants,
                (long) servants * BaseScore.SERVANT_POINTS));
    }

    private static HallOfFameEntryPo po(final String identity,
            final int servants, final long total) {
        final HallOfFameEntryPo po = new HallOfFameEntryPo();
        po.setIdentity(identity);
        po.setPlayerName("Dutch");
        po.setPlayerDisplayName("Player " + identity);
        po.setDifficulty(Difficulty.CONQUISTADOR.name());
        po.setServants(servants);
        po.setColonists(0);
        po.setExperts(0);
        po.setFathers(0);
        po.setGold(0L);
        po.setSupportPercent(0);
        po.setDestroyedSettlements(0);
        po.setBellsBeyondTarget(0L);
        po.setTotal(total);
        return po;
    }

    /** The stored form of an entry written by hand, for an invalid ranking. */
    private static String storedJson(final String identity,
            final int servants) {
        return "{\"identity\":\"" + identity + "\",\"playerName\":\"Dutch\","
                + "\"playerDisplayName\":\"Player\",\"difficulty\":\"CONQUISTADOR\","
                + "\"servants\":" + servants + ",\"colonists\":0,\"experts\":0,"
                + "\"fathers\":0,\"gold\":0,\"supportPercent\":0,"
                + "\"destroyedSettlements\":0,\"bellsBeyondTarget\":0,"
                + "\"total\":" + servants + "}";
    }
}
