package org.microcol.model.campaign.po;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.model.HallOfFame;
import org.microcol.model.HallOfFameEntry;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.store.HallOfFamePo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

/**
 * Reads and writes the persistent top ten of scored results (R-0197) in a file
 * beside the saves. A missing file is an empty ranking. An unreadable or
 * invalid file raises {@link MicroColException} and is left unchanged, and a
 * failed write keeps the previous file, so a stored ranking is never replaced
 * by an empty or partial one.
 */
public final class HallOfFameDao {

    @FunctionalInterface
    interface AtomicMove {
        void move(Path source, Path target) throws IOException;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PersistingTool persistingTool;

    private final AtomicMove atomicMove;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    public HallOfFameDao(final PersistingTool persistingTool) {
        this(persistingTool,
                (source, target) -> Files.move(source, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING));
    }

    HallOfFameDao(final PersistingTool persistingTool,
            final AtomicMove atomicMove) {
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.atomicMove = Preconditions.checkNotNull(atomicMove);
    }

    /**
     * @return the saved ranking, empty while no file has been written
     * @throws MicroColException when the file cannot be read or is not a valid
     *                           ranking
     */
    public HallOfFame load() {
        final File file = persistingTool.getHallOfFameFile();
        if (!file.exists()) {
            return HallOfFame.empty();
        }
        try {
            final HallOfFamePo saved = gson.fromJson(
                    Files.readString(file.toPath(), StandardCharsets.UTF_8),
                    HallOfFamePo.class);
            Preconditions.checkArgument(
                    saved != null && saved.getEntries() != null,
                    "Hall of Fame file has no entries");
            return HallOfFame.of(saved.getEntries().stream()
                    .map(HallOfFameEntry::load).collect(Collectors.toList()));
        } catch (final IOException | RuntimeException invalid) {
            throw new MicroColException(
                    "Hall of Fame file is unreadable or invalid", invalid);
        }
    }

    /**
     * Write the ranking through a temporary file and an atomic move, so that a
     * failed write leaves the previous file in place.
     *
     * @param hall required ranking to store
     * @throws MicroColException when the file cannot be written
     */
    void save(final HallOfFame hall) {
        final HallOfFamePo po = new HallOfFamePo();
        po.setEntries(hall.getEntries().stream().map(HallOfFameEntry::save)
                .collect(Collectors.toList()));
        final Path destination = persistingTool.getHallOfFameFile().toPath()
                .toAbsolutePath();
        Path temporary = null;
        try {
            temporary = Files.createTempFile(destination.getParent(),
                    destination.getFileName().toString() + ".", ".tmp");
            try (final Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(temporary), StandardCharsets.UTF_8)) {
                writer.write(gson.toJson(po));
            }
            atomicMove.move(temporary, destination);
        } catch (final IOException e) {
            throw new MicroColException(e.getMessage(), e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (final IOException cleanupFailure) {
                    logger.warn("Unable to remove unfinished Hall of Fame {}",
                            temporary, cleanupFailure);
                }
            }
        }
    }

    /**
     * Record the frozen result of a game through the path that every scored end
     * shares (R-0193, R-0197): spring 1800, a final end and a confirmed
     * retirement all call it. Recording the same identity again changes
     * nothing, and a game without a frozen result only reads the ranking.
     *
     * @param model required game
     * @return the ranking after the record
     * @throws MicroColException when the saved ranking cannot be read or
     *                           written
     */
    public HallOfFame recordFrozenResult(final Model model) {
        Preconditions.checkNotNull(model, "Model is required");
        return record(model.getHallOfFameEntry());
    }

    /**
     * Record an already captured scored result (R-0197). The terminal dialog
     * captures its entry before the game is left, so the record and the shown
     * breakdown come from the same snapshot.
     *
     * @param entry required snapshot, empty when no result is frozen
     * @return the ranking after the record
     * @throws MicroColException when the saved ranking cannot be read or
     *                           written
     */
    public HallOfFame record(final Optional<HallOfFameEntry> entry) {
        Preconditions.checkNotNull(entry, "Entry is required");
        final HallOfFame current = load();
        if (entry.isEmpty()) {
            return current;
        }
        final HallOfFame next = current.with(entry.get());
        if (next != current) {
            save(next);
        }
        return next;
    }

}
