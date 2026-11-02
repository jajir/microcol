package org.akropia.model.campaign.po;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.util.UUID;

import org.akropia.model.Player;
import org.akropia.model.campaign.po.SavedGames.Kind;

import com.google.common.base.Preconditions;

/**
 * Small description stored as {@code metadata.json} beside the complete game of
 * a saved-game archive (R-0382). Listing reads only this entry.
 */
public class SaveMetadataPo {

    private int formatVersion;
    private String createdWith;
    private String saveName;
    private Kind kind;
    private String savedAt;
    private String playthroughId;
    private SaveSummaryPo summary;

    /** Shown when no build version is packaged, e.g. in a source build. */
    static final String UNKNOWN_BUILD = "unknown";

    private static final String BUILD = buildVersion();

    /**
     * Every value is required; only the preview is optional in an archive.
     *
     * @param saveName      required player or game given name, 1–64 code points
     * @param kind          required kind of the save when written
     * @param savedAt       required real time of the save
     * @param playthroughId required playthrough identity
     * @param summary       required overview of the same state
     * @return metadata of the current format
     * @throws IllegalArgumentException when a value is missing or invalid
     */
    public static SaveMetadataPo of(final String saveName, final Kind kind,
            final Instant savedAt, final String playthroughId,
            final SaveSummaryPo summary) {
        Preconditions.checkArgument(savedAt != null, "Save time is required");
        final SaveMetadataPo out = new SaveMetadataPo();
        out.formatVersion = GameModelPoDao.FORMAT_VERSION;
        out.createdWith = BUILD;
        out.saveName = saveName;
        out.kind = kind;
        out.savedAt = savedAt.toString();
        out.playthroughId = playthroughId;
        out.summary = summary;
        out.validate();
        return out;
    }

    /**
     * Check the required values of written or read metadata. The format version
     * is checked separately as an unsupported, not a damaged, save.
     *
     * @throws IllegalArgumentException when a value is missing or invalid
     */
    void validate() {
        Preconditions.checkArgument(
                createdWith != null && !createdWith.isBlank(),
                "Build identity is required");
        Preconditions.checkArgument(saveName != null, "Save name is required");
        Preconditions.checkArgument(
                saveName.equals(Player.normalizeDisplayName(saveName)),
                "Save name is not normalized");
        Preconditions.checkArgument(kind != null, "Save kind is required");
        Preconditions.checkArgument(savedAt != null, "Save time is required");
        try {
            Instant.parse(savedAt);
        } catch (DateTimeParseException invalid) {
            throw new IllegalArgumentException("Invalid save time", invalid);
        }
        Preconditions.checkArgument(playthroughId != null,
                "Playthrough identity is required");
        Preconditions.checkArgument(
                UUID.fromString(playthroughId).toString().equals(playthroughId),
                "Invalid playthrough identity");
        Preconditions.checkArgument(summary != null, "Summary is required");
        summary.validate();
    }

    /**
     * The packaged version, else the version resource of the game, else
     * {@link #UNKNOWN_BUILD}; never a guessed revision.
     */
    private static String buildVersion() {
        final String packaged = SaveMetadataPo.class.getPackage()
                .getImplementationVersion();
        if (packaged != null && !packaged.isBlank()) {
            return packaged;
        }
        try (InputStream stream = SaveMetadataPo.class.getClassLoader()
                .getResourceAsStream("version.properties")) {
            if (stream != null) {
                final Properties properties = new Properties();
                properties.load(stream);
                final String version = properties.getProperty("version");
                if (version != null && !version.isBlank()
                        && !version.contains("${")) {
                    return version.trim();
                }
            }
        } catch (IOException | IllegalArgumentException unreadable) {
            // The diagnostic identity falls back to unknown.
        }
        return UNKNOWN_BUILD;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public String getCreatedWith() {
        return createdWith;
    }

    public String getSaveName() {
        return saveName;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return the real UTC time of the save
     * @throws DateTimeParseException when the stored time is not ISO 8601
     */
    public Instant getSavedAt() {
        return Instant.parse(savedAt);
    }

    public String getPlaythroughId() {
        return playthroughId;
    }

    public SaveSummaryPo getSummary() {
        return summary;
    }
}
