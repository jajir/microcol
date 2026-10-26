package org.microcol.model.campaign.po;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.microcol.model.MicroColException;
import com.google.inject.Inject;

/** The local save catalogue. Reading an entry never rewrites its game. */
public final class SavedGames {
    public enum Kind {
        MANUAL, QUICK, AUTOMATIC
    }

    public static final class Entry {
        public final File file;
        public final Kind kind;
        public final GameModelPo snapshot;
        public final long modified;

        Entry(final File file, final Kind kind, final GameModelPo snapshot) {
            this.file = file;
            this.kind = kind;
            this.snapshot = snapshot;
            modified = file.lastModified();
        }

        public String getName() {
            return snapshot != null && snapshot.getSaveName() != null
                    ? snapshot.getSaveName()
                    : file.getName();
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
        GameModelPo snapshot;
        try {
            snapshot = reader.loadFromFile(file);
        } catch (RuntimeException invalid) {
            snapshot = null;
        }
        return new Entry(file, kind, snapshot);
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
