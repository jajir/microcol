package org.microcol.model.campaign.po;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.microcol.model.MicroColException;
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

    private final PersistingTool paths;
    private final GameModelPoDao reader;

    @Inject
    public SavedGames(final PersistingTool paths, final GameModelPoDao reader) {
        this.paths = paths;
        this.reader = reader;
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
        final Path directory = paths.getRootSaveDirectory().toPath()
                .resolve("saves")
                .resolve(UUID.fromString(playthroughId).toString());
        try {
            Files.createDirectories(directory);
        } catch (IOException failed) {
            throw new MicroColException("Cannot create save directory", failed);
        }
        return directory.resolve(
                quick ? "quick.microcol" : UUID.randomUUID() + ".microcol")
                .toFile();
    }

    public File quickFile(final String playthroughId) {
        return paths.getRootSaveDirectory().toPath().resolve("saves")
                .resolve(UUID.fromString(playthroughId).toString())
                .resolve("quick.microcol").toFile();
    }
}
