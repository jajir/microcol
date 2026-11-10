package org.akropia.model.campaign.po;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.akropia.model.ConstructionType;
import org.akropia.model.Direction;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.AkropiaException;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.WorldMap;
import org.akropia.model.campaign.po.SaveArchiveException.Reason;
import org.akropia.model.store.GsonConstructionTypeAdapter;
import org.akropia.model.store.GsonDirectionTypeAdapter;
import org.akropia.model.store.GsonGoodsAdapter;
import org.akropia.model.store.GsonGoodsTypeAdapter;
import org.akropia.model.store.GsonQueueItemTypeAdapter;
import org.akropia.model.store.GsonTerrainTypeAdapter;
import org.akropia.model.store.GsonUnitTypeAdapter;
import org.akropia.model.store.QueueItemType;
import org.akropia.model.store.UnitActionPo;
import org.akropia.model.store.UnitActionPoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.inject.Singleton;

/**
 * Read and write the whole game model. Player saves are ZIP archives holding
 * {@code metadata.json}, the persistent model as {@code game.json} and an
 * optional {@code preview.png} (R-0382); authored scenarios stay plain JSON of
 * the same persistent model.
 */
@Singleton
public final class GameModelPoDao {

    /** Version of the whole archive contract, including the game JSON. */
    public static final int FORMAT_VERSION = 1;

    public static final int MAX_PREVIEW_WIDTH = 960;

    public static final int MAX_PREVIEW_HEIGHT = 540;

    static final String METADATA = "metadata.json";

    static final String GAME = "game.json";

    static final String PREVIEW = "preview.png";

    static final long MAX_ARCHIVE_BYTES = 80L << 20;

    static final int MAX_GAME_BYTES = 64 << 20;

    static final int MAX_METADATA_BYTES = 1 << 20;

    static final int MAX_PREVIEW_BYTES = 8 << 20;

    private static final Set<String> ENTRIES = Set.of(METADATA, GAME, PREVIEW);

    private static final byte[] PNG_SIGNATURE = { (byte) 0x89, 'P', 'N', 'G',
            '\r', '\n', 0x1a, '\n' };

    @FunctionalInterface
    interface AtomicMove {
        void move(Path source, Path target) throws IOException;
    }

    @FunctionalInterface
    private interface Content {
        void write(OutputStream out) throws IOException;
    }

    @FunctionalInterface
    private interface ArchiveReader<T> {
        T read(ZipFile zip) throws IOException;
    }

    private final AtomicMove atomicMove;

    private Logger logger = LoggerFactory.getLogger(GameModelPoDao.class);

    private final Gson gson;

    public GameModelPoDao() {
        this((source, target) -> Files.move(source, target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING));
    }

    GameModelPoDao(final AtomicMove atomicMove) {
        this.atomicMove = Preconditions.checkNotNull(atomicMove);
        gson = new GsonBuilder()
                .registerTypeAdapter(ConstructionType.class,
                        new GsonConstructionTypeAdapter())
                .registerTypeAdapter(GoodsType.class,
                        new GsonGoodsTypeAdapter())
                .registerTypeAdapter(QueueItemType.class,
                        new GsonQueueItemTypeAdapter())
                .registerTypeAdapter(TerrainType.class,
                        new GsonTerrainTypeAdapter())
                .registerTypeAdapter(UnitActionPo.class,
                        new UnitActionPoAdapter())
                .registerTypeAdapter(UnitType.class, new GsonUnitTypeAdapter())
                .registerTypeAdapter(Direction.class,
                        new GsonDirectionTypeAdapter().nullSafe())
                .registerTypeAdapter(Goods.class, new GsonGoodsAdapter())
                .setPrettyPrinting().create();
    }

    /**
     * Load predefined model of scenario stored on class path.
     *
     * @param fileName required file name on class path
     * @return loaded model persistent object
     */
    public GameModelPo loadFromClassPath(final String fileName) {
        logger.debug("Starting to read from class path ({})", fileName);
        try (final InputStream is = WorldMap.class
                .getResourceAsStream(fileName)) {
            Preconditions.checkArgument(is != null,
                    "input stream for file (%s) is null", fileName);
            final GameModelPo loaded = gson.fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8),
                    GameModelPo.class);
            Preconditions.checkNotNull(loaded,
                    "Unable to load data from class path %s", fileName);
            return loaded;
        } catch (IOException e) {
            throw new AkropiaException(e.getMessage(), e);
        }
    }

    /**
     * Load plain JSON of the persistent model, the authored scenario format.
     * The same as {@link #loadScenarioFromFile(File)}; it never reads a player
     * save, which {@link #loadPlayerGame(File)} reads.
     *
     * @param file required file object
     * @return loaded model persistent object
     */
    public GameModelPo loadFromFile(final File file) {
        return loadScenarioFromFile(file);
    }

    /**
     * Load the game of a player save. Only a complete archive with valid
     * metadata of the same playthrough is accepted; plain JSON is not a player
     * save.
     *
     * @param file required archive
     * @return loaded model persistent object
     * @throws SaveArchiveException when the archive is damaged or unsupported
     */
    public GameModelPo loadPlayerGame(final File file) {
        checkFile(file);
        return loadArchivedGame(file);
    }

    /**
     * Recognize a player-save ZIP container before choosing an editor reader.
     * This only detects the format; {@link #loadPlayerGame(File)} validates it.
     *
     * @param file required input file
     * @return whether the file starts with a ZIP local-file header
     */
    public boolean isPlayerSaveArchive(final File file) {
        checkFile(file);
        try (InputStream input = new FileInputStream(file)) {
            return Arrays.equals(input.readNBytes(4),
                    new byte[] { 'P', 'K', 3, 4 });
        } catch (IOException failed) {
            throw new AkropiaException(failed.getMessage(), failed);
        }
    }

    /**
     * Load an authored scenario stored as plain JSON of the persistent model.
     *
     * @param file required file object
     * @return loaded model persistent object
     */
    public GameModelPo loadScenarioFromFile(final File file) {
        checkFile(file);
        logger.debug("Starting to read scenario ({})", file.getAbsolutePath());
        try (final FileInputStream fis = new FileInputStream(file)) {
            final GameModelPo loaded = gson.fromJson(
                    new InputStreamReader(fis, StandardCharsets.UTF_8),
                    GameModelPo.class);
            Preconditions.checkArgument(loaded != null,
                    "Unable to load data from file %s", file.getAbsolutePath());
            return loaded;
        } catch (IOException e) {
            throw new AkropiaException(e.getMessage(), e);
        }
    }

    /**
     * Read only the metadata of a saved-game archive. The game JSON is neither
     * read nor parsed.
     *
     * @param file required archive
     * @return validated metadata
     * @throws SaveArchiveException when the archive is damaged or unsupported
     */
    public SaveMetadataPo loadMetadata(final File file) {
        checkFile(file);
        return readArchive(file, this::readMetadata);
    }

    /**
     * Read the bounded preview image of a saved-game archive. A missing,
     * oversized or damaged image is not an error of the save.
     *
     * @param file required archive
     * @return PNG bytes within the preview limits, or empty
     */
    public Optional<byte[]> loadPreview(final File file) {
        try {
            checkFile(file);
            return readArchive(file, zip -> {
                if (zip.getEntry(PREVIEW) == null) {
                    return Optional.<byte[]>empty();
                }
                final byte[] png = read(zip, PREVIEW, MAX_PREVIEW_BYTES);
                return isAllowedPreview(png) ? Optional.of(png)
                        : Optional.<byte[]>empty();
            });
        } catch (RuntimeException unavailable) {
            logger.debug("Preview of {} is unavailable", file, unavailable);
            return Optional.empty();
        }
    }

    /**
     * @param file required archive
     * @return the stored game JSON exactly as written
     * @throws SaveArchiveException when the archive is damaged or unsupported
     */
    public String loadGameJson(final File file) {
        checkFile(file);
        return readArchive(file, zip -> {
            readMetadata(zip);
            return text(read(zip, GAME, MAX_GAME_BYTES));
        });
    }

    /**
     * Write plain JSON of the persistent model, the authored scenario format.
     * The same as {@link #saveScenarioToFile(String, GameModelPo)}; player
     * saves use {@link #savePlayerGame}.
     *
     * @param fileName    required target file
     * @param gameModelPo required persistent model
     */
    public void saveToFile(final String fileName,
            final GameModelPo gameModelPo) {
        saveScenarioToFile(fileName, gameModelPo);
    }

    /**
     * Write a complete player save archive into a temporary file of the target
     * directory and then atomically replace the target. Metadata, including the
     * summary, are required and must describe the same playthrough; only the
     * preview is optional. A failure keeps the previous target.
     *
     * @param fileName    required target file
     * @param gameModelPo required persistent model
     * @param metadata    required metadata of the same state
     * @param preview     optional PNG preview of the same state
     * @throws IllegalArgumentException when the metadata are incomplete or
     *                                  describe another game, before any write
     */
    public void savePlayerGame(final String fileName,
            final GameModelPo gameModelPo, final SaveMetadataPo metadata,
            final byte[] preview) {
        Preconditions.checkNotNull(gameModelPo);
        Preconditions.checkNotNull(metadata);
        metadata.validate();
        checkSameGame(metadata, gameModelPo);
        final String str = gson.toJson(gameModelPo);
        if (logger.isTraceEnabled()) {
            logger.trace(str);
        }
        final byte[] game = str.getBytes(StandardCharsets.UTF_8);
        final byte[] meta = gson.toJson(metadata)
                .getBytes(StandardCharsets.UTF_8);
        if (game.length > MAX_GAME_BYTES || meta.length > MAX_METADATA_BYTES) {
            // Never write a save that could not be read back.
            throw new AkropiaException("Saved game exceeds the size limit");
        }
        final byte[] image;
        if (preview == null || isAllowedPreview(preview)) {
            image = preview;
        } else {
            logger.warn("Saving without a preview outside the limits");
            image = null;
        }
        writeAtomically(fileName, out -> {
            try (ZipOutputStream zip = new ZipOutputStream(out,
                    StandardCharsets.UTF_8)) {
                put(zip, METADATA, meta);
                put(zip, GAME, game);
                if (image != null) {
                    put(zip, PREVIEW, image);
                }
            }
        });
    }

    /**
     * The gameplay state of a persistent model in the production JSON form,
     * compared structurally (R-0387). Only the save name and the focused map
     * square, a view position, are not gameplay.
     *
     * @param gameModelPo required persistent model
     * @return JSON tree of the gameplay state
     */
    public JsonElement gameplayState(final GameModelPo gameModelPo) {
        final JsonElement out = gson.toJsonTree(
                Preconditions.checkNotNull(gameModelPo), GameModelPo.class);
        out.getAsJsonObject().remove("saveName");
        final JsonElement model = out.getAsJsonObject().get("model");
        if (model != null && model.isJsonObject()) {
            model.getAsJsonObject().remove("focusedField");
        }
        return out;
    }

    /**
     * @param gameModelPo required persistent model
     * @return an independent copy in the production JSON form
     */
    public GameModelPo copyOf(final GameModelPo gameModelPo) {
        return gson.fromJson(
                gson.toJsonTree(Preconditions.checkNotNull(gameModelPo),
                        GameModelPo.class),
                GameModelPo.class);
    }

    /**
     * Copy a complete save to another file through the same temporary file and
     * atomic replacement as a new save. A failure keeps the previous target.
     *
     * @param source required complete save
     * @param target required target file
     */
    public void copyPlayerGame(final File source, final File target) {
        checkFile(source);
        writeAtomically(target.getAbsolutePath(),
                out -> Files.copy(source.toPath(), out));
    }

    /**
     * Write an authored scenario as plain JSON of the persistent model.
     *
     * @param fileName    required target file
     * @param gameModelPo required persistent model
     */
    public void saveScenarioToFile(final String fileName,
            final GameModelPo gameModelPo) {
        final String str = gson.toJson(gameModelPo);
        writeAtomically(fileName, out -> {
            try (final OutputStreamWriter writer = new OutputStreamWriter(out,
                    StandardCharsets.UTF_8)) {
                writer.write(str);
            }
        });
    }

    private void writeAtomically(final String fileName, final Content content) {
        final Path destination = new File(fileName).toPath().toAbsolutePath();
        Path temporary = null;
        try {
            temporary = Files.createTempFile(destination.getParent(),
                    destination.getFileName().toString() + ".", ".tmp");
            try (final OutputStream out = Files.newOutputStream(temporary)) {
                content.write(out);
            }
            atomicMove.move(temporary, destination);
        } catch (IOException e) {
            throw new AkropiaException(e.getMessage(), e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException cleanupFailure) {
                    logger.warn("Unable to remove unfinished save {}",
                            temporary, cleanupFailure);
                }
            }
        }
    }

    private GameModelPo loadArchivedGame(final File file) {
        logger.debug("Starting to read saved game ({})",
                file.getAbsolutePath());
        return readArchive(file, zip -> {
            final SaveMetadataPo metadata = readMetadata(zip);
            final GameModelPo loaded;
            try {
                loaded = gson.fromJson(text(read(zip, GAME, MAX_GAME_BYTES)),
                        GameModelPo.class);
            } catch (JsonParseException invalid) {
                throw new SaveArchiveException(Reason.INVALID,
                        "Invalid game data", invalid);
            }
            if (loaded == null) {
                throw new SaveArchiveException(Reason.INVALID,
                        "Missing game data");
            }
            try {
                checkSameGame(metadata, loaded);
            } catch (IllegalArgumentException other) {
                throw new SaveArchiveException(Reason.INVALID,
                        "Metadata describe another game", other);
            }
            return loaded;
        });
    }

    /**
     * The metadata identity and summary keys must be those of the game data.
     */
    private static void checkSameGame(final SaveMetadataPo metadata,
            final GameModelPo game) {
        Preconditions.checkArgument(
                metadata.getPlaythroughId().equals(game.getPlaythroughId()),
                "Metadata describe another playthrough");
        final SaveSummaryPo summary = metadata.getSummary();
        Preconditions.checkArgument(
                summary.getCampaignName() == null || summary.getCampaignName()
                        .equals(game.getCampaignName()),
                "Summary describes another campaign");
        Preconditions.checkArgument(
                summary.getMissionName() == null || summary.getMissionName()
                        .equals(game.getMissionName()),
                "Summary describes another mission");
    }

    private SaveMetadataPo readMetadata(final ZipFile zip) throws IOException {
        final SaveMetadataPo metadata;
        try {
            metadata = gson.fromJson(
                    text(read(zip, METADATA, MAX_METADATA_BYTES)),
                    SaveMetadataPo.class);
        } catch (JsonParseException invalid) {
            throw new SaveArchiveException(Reason.INVALID, "Invalid metadata",
                    invalid);
        }
        if (metadata == null) {
            throw new SaveArchiveException(Reason.INVALID, "Missing metadata");
        }
        if (metadata.getFormatVersion() != FORMAT_VERSION) {
            throw new SaveArchiveException(Reason.UNSUPPORTED,
                    "Unsupported saved-game format "
                            + metadata.getFormatVersion());
        }
        try {
            metadata.validate();
        } catch (IllegalArgumentException invalid) {
            throw new SaveArchiveException(Reason.INVALID,
                    "Incomplete metadata", invalid);
        }
        return metadata;
    }

    /**
     * Open the archive and check its structure before any entry is read:
     * exactly the known root entries, each once, with metadata and game.
     */
    private <T> T readArchive(final File file, final ArchiveReader<T> reader) {
        if (file.length() > MAX_ARCHIVE_BYTES) {
            throw new SaveArchiveException(Reason.INVALID,
                    "Saved game exceeds the size limit");
        }
        try (ZipFile zip = new ZipFile(file, StandardCharsets.UTF_8)) {
            final Set<String> names = new HashSet<>();
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !ENTRIES.contains(entry.getName())
                        || !names.add(entry.getName())) {
                    throw new SaveArchiveException(Reason.INVALID,
                            "Unexpected saved-game entry");
                }
            }
            if (!names.contains(METADATA) || !names.contains(GAME)) {
                throw new SaveArchiveException(Reason.INVALID,
                        "Incomplete saved game");
            }
            return reader.read(zip);
        } catch (ZipException | CharacterCodingException invalid) {
            throw new SaveArchiveException(Reason.INVALID,
                    "Invalid saved-game archive", invalid);
        } catch (IOException e) {
            throw new AkropiaException(e.getMessage(), e);
        }
    }

    /**
     * Read one entry while counting the actual bytes; the size recorded in the
     * archive is not trusted.
     */
    private static byte[] read(final ZipFile zip, final String name,
            final int limit) throws IOException {
        try (InputStream in = zip.getInputStream(zip.getEntry(name))) {
            final byte[] bytes = in.readNBytes(limit + 1);
            if (bytes.length > limit) {
                throw new SaveArchiveException(Reason.INVALID,
                        "Saved-game entry exceeds the size limit");
            }
            return bytes;
        }
    }

    private static String text(final byte[] bytes)
            throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .decode(ByteBuffer.wrap(bytes)).toString();
    }

    private static void put(final ZipOutputStream zip, final String name,
            final byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }

    /**
     * @return whether the bytes start as a PNG whose header declares a size
     *         within the preview limits
     */
    static boolean isAllowedPreview(final byte[] png) {
        if (png.length < 24 || png.length > MAX_PREVIEW_BYTES
                || !Arrays.equals(Arrays.copyOf(png, 8), PNG_SIGNATURE)
                || png[12] != 'I' || png[13] != 'H' || png[14] != 'D'
                || png[15] != 'R') {
            return false;
        }
        final ByteBuffer header = ByteBuffer.wrap(png, 16, 8);
        final int width = header.getInt();
        final int height = header.getInt();
        return width > 0 && height > 0 && width <= MAX_PREVIEW_WIDTH
                && height <= MAX_PREVIEW_HEIGHT;
    }

    private static void checkFile(final File file) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkArgument(file.exists(), "File '%s' doesn't exists.",
                file.getAbsolutePath());
        Preconditions.checkArgument(file.isFile());
    }

}
