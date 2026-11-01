package org.microcol.model.campaign.po;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.microcol.model.MicroColException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * The local save catalogue. Listing reads only the metadata of each archive,
 * the preview is read for one selected entry and the game only when loading.
 * Reading an entry never rewrites its file.
 */
public final class SavedGames {
    public enum Kind {
        MANUAL, QUICK, AUTOMATIC
    }

    public static final class Entry {
        public final File file;
        /**
         * Kind given by the catalogue location. The stored kind of a copied or
         * imported file never grants overwriting a managed slot.
         */
        public final Kind kind;
        /** Metadata, or <code>null</code> when the file is not readable. */
        public final SaveMetadataPo metadata;
        /** Why the file cannot be loaded, or <code>null</code>. */
        public final SaveArchiveException.Reason problem;
        /** Real time of the save in milliseconds since the epoch. */
        public final long modified;

        Entry(final File file, final Kind kind, final SaveMetadataPo metadata,
                final SaveArchiveException.Reason problem) {
            this.file = file;
            this.kind = kind;
            this.metadata = metadata;
            this.problem = problem;
            modified = metadata == null ? file.lastModified()
                    : metadata.getSavedAt().toEpochMilli();
        }

        /**
         * @return the stored name, else the file name without its extension
         */
        public String getName() {
            if (metadata != null && metadata.getSaveName() != null) {
                return metadata.getSaveName();
            }
            final String name = file.getName();
            return name.endsWith(PersistingTool.EXTENSION)
                    ? name.substring(0,
                            name.length() - PersistingTool.EXTENSION.length())
                    : name;
        }
    }

    /** Number of the most recent turn snapshots kept per playthrough. */
    public static final int TURN_HISTORY = 5;

    /**
     * A managed turn snapshot names its write sequence, which orders the
     * history independently of the game date and the clock (R-0388).
     */
    private static final Pattern TURN_SNAPSHOT = Pattern
            .compile("turn-(\\d{10})\\.microcol");

    @FunctionalInterface
    interface Removal {
        void delete(Path file) throws IOException;
    }

    private final PersistingTool paths;
    private final GameModelPoDao reader;
    private final Removal removal;

    @Inject
    public SavedGames(final PersistingTool paths, final GameModelPoDao reader) {
        this(paths, reader, Files::deleteIfExists);
    }

    SavedGames(final PersistingTool paths, final GameModelPoDao reader,
            final Removal removal) {
        this.paths = paths;
        this.reader = reader;
        this.removal = removal;
    }

    public List<Entry> list() {
        final Path root = paths.getRootSaveDirectory().toPath();
        try (Stream<Path> files = Files.walk(root, 3)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".microcol"))
                    .map(path -> read(path.toFile()))
                    .sorted(Comparator
                            .comparingLong((Entry entry) -> entry.modified)
                            .reversed())
                    .collect(Collectors.toList());
        } catch (IOException | java.io.UncheckedIOException failed) {
            throw new MicroColException("Cannot list saved games", failed);
        }
    }

    /**
     * @param entry required listed entry
     * @return its bounded preview image, or empty when it has none or the image
     *         is unusable
     */
    public Optional<byte[]> preview(final Entry entry) {
        return entry.metadata == null ? Optional.empty()
                : reader.loadPreview(entry.file);
    }

    private Entry read(final File file) {
        Kind kind = Kind.MANUAL;
        for (final RecoverySlot slot : RecoverySlot.values()) {
            if (file.equals(paths.getRecoveryFile(slot))) {
                kind = Kind.AUTOMATIC;
            }
        }
        if (file.getName().equals("quick.microcol")
                && !file.getParentFile().equals(paths.getRootSaveDirectory())) {
            kind = Kind.QUICK;
        }
        if (isTurnSnapshot(file)) {
            kind = Kind.AUTOMATIC;
        }
        try {
            return new Entry(file, kind, reader.loadMetadata(file), null);
        } catch (SaveArchiveException invalid) {
            return new Entry(file, kind, null, invalid.getReason());
        } catch (RuntimeException invalid) {
            return new Entry(file, kind, null,
                    SaveArchiveException.Reason.INVALID);
        }
    }

    public File target(final String playthroughId, final boolean quick) {
        final Path directory = createDirectory(playthroughId);
        return directory.resolve(
                quick ? "quick.microcol" : UUID.randomUUID() + ".microcol")
                .toFile();
    }

    public File quickFile(final String playthroughId) {
        return directory(playthroughId).resolve("quick.microcol").toFile();
    }

    /**
     * @param playthroughId required playthrough identity
     * @return a file for the next turn snapshot of the playthrough, after its
     *         newest one; it never names an existing file
     */
    public File nextTurnSnapshot(final String playthroughId) {
        final Path directory = createDirectory(playthroughId);
        final long next = turnSnapshots(playthroughId).stream()
                .mapToLong(SavedGames::sequence).max().orElse(0) + 1;
        final File out = directory
                .resolve(String.format("turn-%010d.microcol", next)).toFile();
        if (out.exists()) {
            throw new MicroColException("Turn snapshot already exists");
        }
        return out;
    }

    /**
     * Only the managed snapshot names of the playthrough's own directory are
     * listed; other save categories and imported files never are.
     *
     * @param playthroughId required playthrough identity
     * @return turn snapshots of the playthrough, oldest write first
     */
    public List<File> turnSnapshots(final String playthroughId) {
        final Path directory = directory(playthroughId);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).map(Path::toFile)
                    .filter(SavedGames::isTurnSnapshotName)
                    .sorted(Comparator.comparingLong(SavedGames::sequence))
                    .collect(Collectors.toList());
        } catch (IOException | java.io.UncheckedIOException failed) {
            throw new MicroColException("Cannot list turn snapshots", failed);
        }
    }

    /**
     * Remove the oldest turn snapshots beyond {@link #TURN_HISTORY}. A file
     * that cannot be removed stays and is retried by a later cleanup; no other
     * snapshot is removed in its place.
     *
     * @param playthroughId required playthrough identity
     * @return <code>true</code> when the history is within the limit
     */
    public boolean pruneTurnSnapshots(final String playthroughId) {
        final List<File> snapshots = turnSnapshots(playthroughId);
        boolean complete = true;
        for (final File old : snapshots.subList(0,
                Math.max(0, snapshots.size() - TURN_HISTORY))) {
            try {
                removal.delete(old.toPath());
            } catch (IOException | SecurityException failed) {
                complete = false;
            }
        }
        return complete;
    }

    private boolean isTurnSnapshot(final File file) {
        final File parent = file.getParentFile();
        return isTurnSnapshotName(file) && parent != null
                && parent.getParentFile() != null
                && parent.getParentFile().toPath().equals(
                        paths.getRootSaveDirectory().toPath().resolve("saves"));
    }

    private static boolean isTurnSnapshotName(final File file) {
        return TURN_SNAPSHOT.matcher(file.getName()).matches();
    }

    private static long sequence(final File file) {
        final Matcher matcher = TURN_SNAPSHOT.matcher(file.getName());
        Preconditions.checkArgument(matcher.matches());
        return Long.parseLong(matcher.group(1));
    }

    private Path directory(final String playthroughId) {
        return paths.getRootSaveDirectory().toPath().resolve("saves")
                .resolve(UUID.fromString(playthroughId).toString());
    }

    private Path createDirectory(final String playthroughId) {
        final Path directory = directory(playthroughId);
        try {
            Files.createDirectories(directory);
        } catch (IOException failed) {
            throw new MicroColException("Cannot create save directory", failed);
        }
        return directory;
    }
}
