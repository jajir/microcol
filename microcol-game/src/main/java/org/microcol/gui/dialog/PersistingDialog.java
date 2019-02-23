package org.microcol.gui.dialog;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import org.microcol.gui.FileSelectingService;
import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.PersistingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Perform load and save operations. When it's necessary it shows system file
 * dialog.
 */
public class PersistingDialog {

    public static final String SAVE_FILE_EXTENSION = "microcol";

    private final Logger logger = LoggerFactory.getLogger(PersistingDialog.class);

    private final FileSelectingService fileSelectingService;

    private final GameController gameController;

    private final PersistingTool persistingTool;

    @Inject
    public PersistingDialog(final GameController gameController,
            final PersistingTool persistingTool, final FileSelectingService fileSelectingService) {
        this.gameController = Preconditions.checkNotNull(gameController);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.fileSelectingService = Preconditions.checkNotNull(fileSelectingService);
    }

    public void saveModel() {
        final File saveFile = fileSelectingService.saveFile(persistingTool.getRootSaveDirectory(),
                persistingTool.getSuggestedSaveFileName());
        if (saveFile == null) {
            logger.debug("User didn't select any file to save game");
        } else {
            saveModelToFile(saveFile);
        }
    }

    public void loadModel() {
        final File saveFile = fileSelectingService.loadFile(persistingTool.getRootSaveDirectory());
        if (saveFile == null) {
            logger.debug("User didn't select any file to load game");
        } else {
            loadFromFile(saveFile);
        }
    }

    private void saveModelToFile(final File targetFile) {
        final File file = correctFileNameExtension(targetFile);
        logger.debug("Saving game to file '{}' ", file.getAbsolutePath());
        writeModelToFile(file);
    }

    private void loadFromFile(final File sourceFile) {
        final File file = correctFileNameExtension(sourceFile);
        logger.debug("Loading game from file '{}' ", file.getAbsolutePath());
        loadModelFromFile(file);
    }

    private File correctFileNameExtension(final File targetFile) {
        Preconditions.checkNotNull(targetFile);
        final Path path = targetFile.toPath();
        final String fileName = getFileName(path);
        if (fileName.toLowerCase(Locale.getDefault()).endsWith(SAVE_FILE_EXTENSION)) {
            return targetFile;
        } else {
            final Path parent = path.getParent();
            if (parent == null) {
                throw new MicroColException(
                        String.format("Unable to determine paret path of '%s'", path));
            }
            Path out = parent.resolve(fileName + "." + SAVE_FILE_EXTENSION);
            return out.toFile();
        }
    }

    private String getFileName(final Path path) {
        Preconditions.checkNotNull(path);
        final Path fileName = path.getFileName();
        if (fileName == null) {
            throw new MicroColException(
                    String.format("Unable to get file name from path '%s' ", path));
        } else {
            return fileName.toString();
        }
    }

    private void writeModelToFile(final File targetFile) {
        gameController.writeModelToFile(targetFile);
    }

    private void loadModelFromFile(final File sourceFile) {
        gameController.loadModelFromFile(sourceFile);
    }

}
