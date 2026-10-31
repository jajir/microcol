package org.microcol.gui.dialog;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import org.microcol.diagnostics.DiagnosticEvent;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.TurnPreparationException;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.campaign.po.PersistingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Perform load and save operations. When it's necessary it shows system file
 * dialog.
 */
@com.google.inject.Singleton
public class PersistingService {

    private final org.microcol.model.campaign.po.SavedGames savedGames;
    private final org.microcol.gui.util.ViewUtil view;
    private final org.microcol.i18n.I18n texts;
    private final org.microcol.gui.preferences.GamePreferences preferences;
    private final Logger logger = LoggerFactory
            .getLogger(PersistingService.class);

    private final FileSelectingService fileSelectingService;

    private final GameController gameController;

    private final PersistingTool persistingTool;

    private final EventBus eventBus;
    private final GameModelController gameModelController;

    private final DiagnosticLog diagnosticLog;

    @Inject
    public PersistingService(final GameController gameController,
            final EventBus eventBus, final PersistingTool persistingTool,
            final FileSelectingService fileSelectingService,
            final GameModelController gameModelController,
            final DiagnosticLog diagnosticLog,
            final org.microcol.model.campaign.po.SavedGames savedGames,
            final org.microcol.gui.util.ViewUtil view,
            final org.microcol.i18n.I18n texts,
            final org.microcol.gui.preferences.GamePreferences preferences) {
        this.savedGames = savedGames;
        this.view = view;
        this.texts = texts;
        this.preferences = preferences;
        this.diagnosticLog = Preconditions.checkNotNull(diagnosticLog);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.fileSelectingService = Preconditions
                .checkNotNull(fileSelectingService);
    }

    public void saveModel(final Model model) {
        final File saveFile;
        try {
            saveFile = fileSelectingService.saveFile(
                    persistingTool.getRootSaveDirectory(),
                    persistingTool.getSuggestedSaveFileName(model));
        } catch (MicroColException unavailable) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, unavailable);
            gameModelController.showRecoverySaveFailure();
            return;
        }
        if (saveFile == null) {
            logger.debug("User didn't select any file to save game");
        } else {
            saveModelToFile(saveFile);
        }
    }

    /**
     * Show file dialog to player and allows him to choose file to load.
     *
     * @param isSecretOptionEnabled it's <code>true</code> when player press
     *                              secret option
     */
    public void loadFromSavedGames(boolean isSecretOptionEnabled) {
        if (isSecretOptionEnabled) {
            importGame(true);
        } else {
            browse(false, false);
        }
    }

    private void importGame(final boolean isSecretOptionEnabled) {
        final File saveFile;
        try {
            saveFile = fileSelectingService
                    .loadFile(persistingTool.getRootSaveDirectory());
        } catch (MicroColException unavailable) {
            diagnosticLog.record(DiagnosticEvent.LOAD_FAILED, unavailable);
            gameModelController.showSaveFolderUnavailable();
            return;
        }
        if (saveFile == null) {
            logger.debug("User didn't select any file to load game");
        } else {
            if (isSecretOptionEnabled) {
                eventBus.post(new ShowScreenEvent(Screen.EDITOR, saveFile));
            } else if (loadFromFile(saveFile)) {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
            }
        }
    }

    private void saveModelToFile(final File targetFile) {
        final File file = correctFileNameExtension(targetFile);
        logger.debug("Saving game to file '{}' ", file.getAbsolutePath());
        writeModelToFile(file);
    }

    private boolean loadFromFile(final File sourceFile) {
        final File file = correctFileNameExtension(sourceFile);
        logger.debug("Loading game from file '{}' ", file.getAbsolutePath());
        final boolean loaded = gameController.loadModelFromFile(file,
                this::confirmLeave);
        if (loaded) {
            preferences.setGameInProgressSaveFile(file.getAbsolutePath());
        }
        return loaded;
    }

    /**
     * Add correct file extension to file.
     *
     * @param targetFile required file
     * @return corrected file
     */
    private File correctFileNameExtension(final File targetFile) {
        Preconditions.checkNotNull(targetFile);
        final Path path = targetFile.toPath();
        final String fileName = getFileName(path);
        if (fileName.toLowerCase(Locale.getDefault())
                .endsWith(PersistingTool.SAVE_FILE_EXTENSION)) {
            return targetFile;
        } else {
            final Path parent = path.getParent();
            if (parent == null) {
                throw new MicroColException(String.format(
                        "Unable to determine paret path of '%s'", path));
            }
            Path out = parent.resolve(
                    fileName + "." + PersistingTool.SAVE_FILE_EXTENSION);
            return out.toFile();
        }
    }

    private String getFileName(final Path path) {
        Preconditions.checkNotNull(path);
        final Path fileName = path.getFileName();
        if (fileName == null) {
            throw new MicroColException(String
                    .format("Unable to get file name from path '%s' ", path));
        } else {
            return fileName.toString();
        }
    }

    private boolean writeModelToFile(final File targetFile) {
        try {
            gameController.writeModelToFile(targetFile);
            preferences.setGameInProgressSaveFile(targetFile.getAbsolutePath());
            feedback(SavedGameText.saved);
            return true;
        } catch (TurnPreparationException rejected) {
            diagnosticLog.record(DiagnosticEvent.TURN_PREPARATION_REJECTED,
                    rejected);
            gameModelController.showTurnPreparationUnavailable();
        } catch (MicroColException failed) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, failed);
            gameModelController.showRecoverySaveFailure();
        }
        return false;
    }

    public void browse(final boolean saving, final boolean recoveryOnly) {
        if (saving && !gameModelController.isHumanTurnReady()) {
            return;
        }
        while (true) {
            final SavedGamesDialog.Choice choice;
            try {
                choice = new SavedGamesDialog(view, texts).choose(saving,
                        savedGames.list(), recoveryOnly);
            } catch (MicroColException failed) {
                gameModelController.showSaveFolderUnavailable();
                return;
            }
            if (choice == null) {
                return;
            }
            try {
                switch (choice.action) {
                case SAVE:
                    if (saveNamed(savedGames.target(gameModelController
                            .getGameModel().getPlaythroughId(), false),
                            choice.name)) {
                        return;
                    }
                    break;
                case LOAD:
                    if (loadFromFile(choice.entry.file)) {
                        eventBus.post(new ShowScreenEvent(Screen.GAME));
                    }
                    return;
                case OVERWRITE:
                    if (confirm(texts.get(SavedGameText.overwrite) + "\n"
                            + choice.entry.getName())
                            && saveNamed(choice.entry.file,
                                    choice.entry.getName())) {
                        return;
                    }
                    break;
                case DELETE:
                    if (confirm(texts.get(SavedGameText.delete) + "\n"
                            + choice.entry.getName())) {
                        java.nio.file.Files.delete(choice.entry.file.toPath());
                    }
                    break;
                case IMPORT:
                    importGame(false);
                    return;
                case EXPORT:
                    saveModel(gameModelController.getModel());
                    return;
                case FOLDER:
                    java.awt.Desktop.getDesktop()
                            .open(persistingTool.getRootSaveDirectory());
                    break;
                default:
                    throw new IllegalStateException(
                            "Unknown saved-game action");
                }
            } catch (java.io.IOException | UnsupportedOperationException
                    | SecurityException | MicroColException failed) {
                diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, failed);
                final DialogMessage message = new DialogMessage(view, texts);
                message.setText(texts.get(SavedGameText.failed));
                message.showAndWait();
            }
        }
    }

    private boolean saveNamed(final File file, final String name) {
        try {
            gameController.writeNamedGame(file, name);
            preferences.setGameInProgressSaveFile(file.getAbsolutePath());
            feedback(SavedGameText.saved);
            return true;
        } catch (TurnPreparationException rejected) {
            gameModelController.showTurnPreparationUnavailable();
        } catch (MicroColException failed) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, failed);
            gameModelController.showRecoverySaveFailure();
        } catch (IllegalArgumentException invalidName) {
            final DialogMessage message = new DialogMessage(view, texts);
            message.setText(texts.get(SavedGameText.invalidName));
            message.showAndWait();
        }
        return false;
    }

    public boolean confirmLeave() {
        if (!gameModelController.isGameModelReady()
                || gameModelController.getModel().isGameFinished()) {
            return true;
        }
        final int choice = new SaveConfirmation(view, texts)
                .choose(texts.get(SavedGameText.leave), true);
        if (choice == 2) {
            return true;
        }
        if (choice != 1) {
            return false;
        }
        try {
            return writeModelToFile(persistingTool.getAutoSaveFile());
        } catch (MicroColException failed) {
            gameModelController.showRecoverySaveFailure();
            return false;
        }
    }

    private boolean confirm(final String message) {
        return new SaveConfirmation(view, texts).choose(message, false) == 1;
    }

    public void quickSave() {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        try {
            saveNamed(
                    savedGames.target(gameModelController.getGameModel()
                            .getPlaythroughId(), true),
                    texts.get(SavedGameText.quick));
        } catch (MicroColException failed) {
            gameModelController.showRecoverySaveFailure();
        }
    }

    public void quickLoad() {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        try {
            final File file = savedGames.quickFile(
                    gameModelController.getGameModel().getPlaythroughId());
            if (!file.isFile()) {
                feedback(SavedGameText.noQuick);
                return;
            }
            if (loadFromFile(file)) {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
            }
        } catch (MicroColException failed) {
            gameModelController.showSaveFolderUnavailable();
        }
    }

    public void continueGame() {
        if (loadFromFile(continueFile())) {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        }
    }

    public File continueFile() {
        final String value = preferences.getGameInProgressSaveFile()
                .orElse("true");
        return "true".equals(value) ? persistingTool.getAutoSaveFile()
                : new File(value);
    }

    public void restartMission() {
        if (gameController.restartMission(this::confirmLeave)) {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        }
    }

    private void feedback(final SavedGameText text) {
        eventBus.post(
                new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                        texts.get(text),
                        org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME));
    }
}
