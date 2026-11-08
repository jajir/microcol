package org.akropia.model.campaign.po;

import java.io.File;

import org.akropia.model.Calendar;
import org.akropia.model.AkropiaException;
import org.akropia.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Service help with files for game saves.
 */
public class PersistingTool {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String SYSTEM_PROPERTY_USER_HOME = "user.home";

    private final static String SAVE_DIRECTORY = ".akropia";

    public static final String SAVE_FILE_EXTENSION = "akropia";

    final static String EXTENSION = "." + SAVE_FILE_EXTENSION;

    private final static String HALL_OF_FAME_FILE_NAME = "hall-of-fame.json";

    @Inject
    public PersistingTool() {
    }

    /**
     * Get base directory where could be file saves.
     *
     * @return return root directory
     */
    /**
     * Get base directory where could be file saves. Unusable locations raise
     * {@link AkropiaException} so that save, exit and load report the failure
     * like other rejected persistence.
     *
     * @return return root directory
     */
    public File getRootSaveDirectory() {
        final File userDir = new File(
                System.getProperty(SYSTEM_PROPERTY_USER_HOME));
        if (!userDir.isDirectory()) {
            throw new AkropiaException("User home is not a directory");
        }

        final File out = userDir.toPath().resolve(SAVE_DIRECTORY).toFile();
        if (!out.exists()) {
            logger.info("creating akropia save directory at '{}'",
                    out.getAbsolutePath());
            if (!out.mkdir()) {
                throw new AkropiaException(
                        String.format("Unable to create save directory (%s)",
                                out.getAbsolutePath()));
            }
        }

        if (!out.isDirectory()) {
            throw new AkropiaException(
                    String.format("Save directory is not a directory (%s)",
                            out.getAbsolutePath()));
        }
        return out;
    }

    /** Shared player-save directory; settings remain in the parent. */
    public File getSaveDirectory() {
        final File out = getRootSaveDirectory().toPath().resolve("saves")
                .toFile();
        if (!out.isDirectory() && !out.mkdirs()) {
            throw new AkropiaException("Cannot create save directory");
        }
        return out;
    }

    public File getAutoSaveFile() {
        return getRecoveryFile(RecoverySlot.LATEST_POSITION);
    }

    public File getRecoveryFile(final RecoverySlot slot) {
        return getRootSaveDirectory().toPath()
                .resolve(Preconditions.checkNotNull(slot).getFileName())
                .toFile();
    }

    /**
     * @return the file of the persistent top ten of scored results (R-0197),
     *         kept beside the saves and not among them
     */
    public File getHallOfFameFile() {
        return getRootSaveDirectory().toPath().resolve(HALL_OF_FAME_FILE_NAME)
                .toFile();
    }

    /**
     * Provide file name that should store current model state.
     *
     * @param model required model
     * @return suggested file name
     */
    public String getSuggestedSaveFileName(final Model model) {
        Preconditions.checkNotNull(model);
        return prepareFileName(
                org.akropia.model.EuropeanNation
                        .findByPlayerName(model.getCurrentPlayer().getName())
                        .map(nation -> nation.name()
                                .toLowerCase(java.util.Locale.ROOT))
                        .orElse(model.getCurrentPlayer().getName()),
                model.getCalendar());
    }

    private String prepareFileName(final String playerName,
            final Calendar calendar) {
        final StringBuilder buff = new StringBuilder();

        buff.append(playerName);
        buff.append('-');
        buff.append(calendar.getCurrentYear());

        if (calendar.getCurrentSeason().isPresent()) {
            buff.append('-');
            buff.append(calendar.getCurrentSeason().get().name());
        }

        buff.append(EXTENSION);

        return buff.toString();
    }

}
