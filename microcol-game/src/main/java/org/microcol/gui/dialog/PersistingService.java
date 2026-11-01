package org.microcol.gui.dialog;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.microcol.diagnostics.DiagnosticEvent;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.SavePreview;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.TurnPreparationException;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.SaveSummaryPo;
import org.microcol.model.campaign.po.SavedGames;
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

    private boolean busy;

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
        busy = true;
        try {
            saveFile = fileSelectingService.saveFile(
                    persistingTool.getRootSaveDirectory(),
                    persistingTool.getSuggestedSaveFileName(model));
        } catch (MicroColException unavailable) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, unavailable);
            gameModelController.showRecoverySaveFailure();
            return;
        } finally {
            busy = false;
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

    private boolean importGame(final boolean isSecretOptionEnabled) {
        final File saveFile;
        busy = true;
        try {
            saveFile = fileSelectingService
                    .loadFile(persistingTool.getRootSaveDirectory());
        } catch (MicroColException unavailable) {
            diagnosticLog.record(DiagnosticEvent.LOAD_FAILED, unavailable);
            gameModelController.showSaveFolderUnavailable();
            return false;
        } finally {
            busy = false;
        }
        if (saveFile == null) {
            logger.debug("User didn't select any file to load game");
        } else {
            if (isSecretOptionEnabled) {
                eventBus.post(new ShowScreenEvent(Screen.EDITOR, saveFile));
                return true;
            } else if (loadFromFile(saveFile)) {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
                return true;
            }
        }
        return false;
    }

    private void saveModelToFile(final File targetFile) {
        final File file = correctFileNameExtension(targetFile);
        logger.debug("Saving game to file '{}' ", file.getAbsolutePath());
        writeModelToFile(file, SavedGames.Kind.MANUAL);
    }

    private boolean loadFromFile(final File sourceFile) {
        final File file = correctFileNameExtension(sourceFile);
        logger.debug("Loading game from file '{}' ", file.getAbsolutePath());
        final boolean loaded;
        busy = true;
        try {
            loaded = gameController.loadModelFromFile(file, this::confirmLeave);
        } finally {
            busy = false;
        }
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

    private boolean writeModelToFile(final File targetFile,
            final SavedGames.Kind kind) {
        try {
            gameController.writeModelToFile(targetFile, kind);
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

    /**
     * Saving uses the modal browser; loading opens the main-window Load page,
     * which returns to the screen that opened it.
     */
    public void browse(final boolean saving, final boolean recoveryOnly) {
        if (!saving) {
            eventBus.post(new ShowScreenEvent(Screen.LOAD, recoveryOnly));
            return;
        }
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        // The detail shows the state a new save would store.
        final var game = gameModelController.getGameModel();
        final SaveSummaryPo current = game.getSaveSummary();
        final byte[] currentImage = SavePreview.render(game.getModel());
        final String suggestedName = suggestedName(current);
        while (true) {
            final List<SavedGames.Entry> entries = listSavedGames();
            if (entries == null) {
                return;
            }
            final SavedGamesDialog.Choice choice = new SavedGamesDialog(view,
                    texts).chooseSave(entries, this::preview, current,
                            currentImage, suggestedName);
            if (choice == null || perform(choice)) {
                return;
            }
        }
    }

    /**
     * @param entry required listed save
     * @return its bounded preview, read only for the selected save
     */
    public Optional<byte[]> preview(final SavedGames.Entry entry) {
        return savedGames.preview(entry);
    }

    private String suggestedName(final SaveSummaryPo current) {
        String out = current.getPlayerName() + " " + current.getYear();
        if (current.getSeason() != null) {
            out += " " + texts.get(org.microcol.gui.Loc
                    .getTerrainDescription(org.microcol.model.Calendar.Season
                            .valueOf(current.getSeason())));
        }
        return out;
    }

    private String describeTarget(final SavedGames.Entry entry) {
        return entry.getName() + "\n"
                + SavedGamesView.formatTime(texts, entry.modified);
    }

    /**
     * @return current saved games, or <code>null</code> after reporting an
     *         unavailable save folder
     */
    public List<SavedGames.Entry> listSavedGames() {
        try {
            return savedGames.list();
        } catch (MicroColException failed) {
            gameModelController.showSaveFolderUnavailable();
            return null;
        }
    }

    /**
     * Perform one saved-games browser action. Failures are reported and keep
     * the browser, the live game and the files.
     *
     * @param choice required chosen action
     * @return <code>true</code> when the action completed the browser
     */
    public boolean perform(final SavedGamesDialog.Choice choice) {
        try {
            switch (choice.action) {
            case SAVE:
                return saveNamed(
                        savedGames.target(gameModelController.getGameModel()
                                .getPlaythroughId(), false),
                        choice.name, SavedGames.Kind.MANUAL);
            case LOAD:
                if (choice.entry.metadata == null) {
                    return false;
                }
                if (loadFromFile(choice.entry.file)) {
                    eventBus.post(new ShowScreenEvent(Screen.GAME));
                    return true;
                }
                return false;
            case OVERWRITE:
                // Only a catalogued manual save is a target; its stored kind
                // grants nothing.
                if (choice.entry == null
                        || choice.entry.kind != SavedGames.Kind.MANUAL) {
                    return false;
                }
                return confirm(texts.get(SavedGameText.overwrite) + "\n"
                        + describeTarget(choice.entry))
                        && saveNamed(choice.entry.file, choice.entry.getName(),
                                SavedGames.Kind.MANUAL);
            case DELETE:
                if (choice.entry == null
                        || choice.entry.kind != SavedGames.Kind.MANUAL) {
                    return false;
                }
                if (confirm(texts.get(SavedGameText.delete) + "\n"
                        + describeTarget(choice.entry))) {
                    java.nio.file.Files.delete(choice.entry.file.toPath());
                }
                return false;
            case IMPORT:
                return importGame(false);
            case EXPORT:
                saveModel(gameModelController.getModel());
                return true;
            case FOLDER:
                java.awt.Desktop.getDesktop()
                        .open(persistingTool.getRootSaveDirectory());
                return false;
            default:
                throw new IllegalStateException("Unknown saved-game action");
            }
        } catch (java.io.IOException | UnsupportedOperationException
                | SecurityException | MicroColException failed) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, failed);
            final DialogMessage message = new DialogMessage(view, texts);
            message.setText(texts.get(SavedGameText.failed));
            message.showAndWait();
            return false;
        }
    }

    /**
     * @return <code>true</code> while a file chooser, load or save is in
     *         progress
     */
    public boolean isBusy() {
        return busy;
    }

    private boolean saveNamed(final File file, final String name,
            final SavedGames.Kind kind) {
        try {
            gameController.writeNamedGame(file, name, kind);
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
            return writeModelToFile(persistingTool.getAutoSaveFile(),
                    SavedGames.Kind.AUTOMATIC);
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
                    texts.get(SavedGameText.quick), SavedGames.Kind.QUICK);
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
