package org.akropia.model.campaign.po;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.text.Normalizer;
import java.util.Locale;
import org.akropia.model.Player;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.akropia.model.AkropiaException;
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

    /** Number of shared circular turn-save slots. */
    public static final int TURN_HISTORY = 10;
    private static final Pattern TURN_SNAPSHOT = Pattern
            .compile("autosave-00[0-9]\\.akropia");
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
            final List<Entry> entries = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".akropia"))
                    .map(path -> read(path.toFile()))
                    .sorted(Comparator
                            .comparingLong((Entry entry) -> entry.modified)
                            .reversed())
                    .collect(Collectors.toList());
            // Wall-clock changes must not reverse the circular history in the
            // browser.
            final var ordered = entries.stream()
                    .filter(entry -> isManagedSnapshot(entry)).sorted(
                            Comparator
                                    .comparingLong(
                                            (Entry entry) -> entry.metadata
                                                    .getAutosaveSequence())
                                    .reversed())
                    .iterator();
            for (int i = 0; i < entries.size(); i++) {
                if (isManagedSnapshot(entries.get(i))) {
                    entries.set(i, ordered.next());
                }
            }
            return entries;
        } catch (IOException | java.io.UncheckedIOException failed) {
            throw new AkropiaException("Cannot list saved games", failed);
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
        if (file.toPath().equals(directory().resolve("quick.akropia"))) {
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

    /** Resolve a portable manual name, including case/Unicode collisions. */
    public File target(final String name) {
        final String filename = manualName(name) + PersistingTool.EXTENSION;
        final Path directory = paths.getSaveDirectory().toPath();
        try (Stream<Path> files = Files.list(directory)) {
            final List<Path> matches = files
                    .filter(path -> key(path.getFileName().toString())
                            .equals(key(filename)))
                    .collect(Collectors.toList());
            Preconditions.checkArgument(matches.size() <= 1,
                    "Ambiguous save filename");
            return matches.isEmpty() ? directory.resolve(filename).toFile()
                    : matches.get(0).toFile();
        } catch (IOException failed) {
            throw new AkropiaException("Cannot resolve save filename", failed);
        }
    }

    /** The name stored in metadata matches the user-visible filename stem. */
    public static String manualName(final String input) {
        String name = Player.normalizeDisplayName(input);
        if (name.toLowerCase(Locale.ROOT).endsWith(PersistingTool.EXTENSION)) {
            name = Player.normalizeDisplayName(name.substring(0,
                    name.length() - PersistingTool.EXTENSION.length()));
        }
        Preconditions.checkArgument(
                !name.matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*")
                        && !name.endsWith(".") && !name.endsWith(" ")
                        && !name.equals(".") && !name.equals(".."),
                "Invalid filename");
        final String stem = name.split("\\.", 2)[0].toLowerCase(Locale.ROOT);
        Preconditions.checkArgument(!stem
                .matches("con|prn|aux|nul|com[1-9]|lpt[1-9]")
                && !name.toLowerCase(Locale.ROOT).matches(
                        "autosave(?:-[0-9]+)?|quick|latest-turn|previous-decade"),
                "Reserved filename");
        return name;
    }

    private static String key(final String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    /** Quick saving is an explicit player action, outside automatic history. */
    public File quickFile() {
        return paths.getSaveDirectory().toPath().resolve("quick.akropia")
                .toFile();
    }

    /**
     * Successful ordering travels atomically with the archive, not a sidecar.
     */
    public long nextSequence() {
        try {
            return Math.addExact(turnSnapshots().stream().mapToLong(
                    file -> reader.loadMetadata(file).getAutosaveSequence())
                    .max().orElse(0), 1);
        } catch (ArithmeticException exhausted) {
            throw new AkropiaException("Autosave sequence exhausted",
                    exhausted);
        }
    }

    public File nextTurnSnapshot(final long sequence) {
        Preconditions.checkArgument(sequence > 0, "Invalid autosave sequence");
        return paths
                .getSaveDirectory().toPath().resolve(String.format(Locale.ROOT,
                        "autosave-%03d.akropia", (sequence - 1) % TURN_HISTORY))
                .toFile();
    }

    /** Valid managed archives, oldest successful write first. */
    public List<File> turnSnapshots() {
        if (!Files.isDirectory(directory())) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory())) {
            return files.filter(Files::isRegularFile).map(Path::toFile)
                    .filter(this::isTurnSnapshot).map(this::read)
                    .filter(this::isManagedSnapshot)
                    .sorted(Comparator.comparingLong(
                            entry -> entry.metadata.getAutosaveSequence()))
                    .map(entry -> entry.file).collect(Collectors.toList());
        } catch (IOException failed) {
            throw new AkropiaException("Cannot list turn snapshots", failed);
        }
    }

    private boolean isManagedSnapshot(final Entry entry) {
        return isTurnSnapshot(entry.file) && entry.metadata != null
                && entry.metadata.getKind() == Kind.AUTOMATIC
                && entry.metadata.getAutosaveSequence() > 0;
    }

    private boolean isTurnSnapshot(final File file) {
        return file.getParentFile().toPath().equals(directory())
                && TURN_SNAPSHOT.matcher(file.getName()).matches();
    }

    private Path directory() {
        return paths.getRootSaveDirectory().toPath().resolve("saves");
    }
}
