package org.microcol.model.campaign.po;

import java.io.File;

import org.microcol.model.Calendar;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
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

    private final static String SAVE_DIRECTORY = ".microcol";

    public static final String SAVE_FILE_EXTENSION = "microcol";

    private final static String EXTENSION = "." + SAVE_FILE_EXTENSION;

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
     * {@link MicroColException} so that save, exit and load report the failure
     * like other rejected persistence.
     *
     * @return return root directory
     */
    public File getRootSaveDirectory() {
        final File userDir = new File(
                System.getProperty(SYSTEM_PROPERTY_USER_HOME));
        if (!userDir.isDirectory()) {
            throw new MicroColException("User home is not a directory");
        }

        final File out = userDir.toPath().resolve(SAVE_DIRECTORY).toFile();
        if (!out.exists()) {
            logger.info("creating microCol save directory at '{}'",
                    out.getAbsolutePath());
            if (!out.mkdir()) {
                throw new MicroColException(
                        String.format("Unable to create save directory (%s)",
                                out.getAbsolutePath()));
            }
        }

        if (!out.isDirectory()) {
            throw new MicroColException(
                    String.format("Save directory is not a directory (%s)",
                            out.getAbsolutePath()));
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
     * Provide file name that should store current model state.
     *
     * @param model required model
     * @return suggested file name
     */
    public String getSuggestedSaveFileName(final Model model) {
        Preconditions.checkNotNull(model);
        return prepareFileName(model.getCurrentPlayer().getName(),
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
