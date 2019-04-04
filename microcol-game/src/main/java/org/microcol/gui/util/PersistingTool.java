package org.microcol.gui.util;

import java.io.File;

import org.microcol.model.Calendar;
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

    private final static String EXTENSION = ".microcol";

    private final static String AUTOSAVE_FILE_NAME = "autosave.microcol";

    @Inject
    public PersistingTool() {
    }

    /**
     * Get base directory where could be file saves.
     * 
     * @return return root directory
     */
    public File getRootSaveDirectory() {
        final File userDir = new File(System.getProperty(SYSTEM_PROPERTY_USER_HOME));
        Preconditions.checkState(userDir.exists());
        Preconditions.checkState(userDir.isDirectory());

        final File out = userDir.toPath().resolve(SAVE_DIRECTORY).toFile();
        if (!out.exists()) {
            logger.info("creating microCol save directory at '{}'", out.getAbsolutePath());
            Preconditions.checkState(out.mkdir(), "Unable to create file (%s)",
                    out.getAbsolutePath());
        }

        Preconditions.checkState(out.exists());
        Preconditions.checkState(out.isDirectory());
        return out;
    }

    public File getAutoSaveFile() {
        return getRootSaveDirectory().toPath().resolve(AUTOSAVE_FILE_NAME).toFile();
    }

    /**
     * Provide file name that should store current model state.
     *
     * @param model
     *            required model
     * @return suggested file name
     */
    public String getSuggestedSaveFileName(final Model model) {
        Preconditions.checkNotNull(model);
        return prepareFileName(model.getCurrentPlayer().getName(), model.getCalendar());
    }

    private String prepareFileName(final String playerName, final Calendar calendar) {
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
