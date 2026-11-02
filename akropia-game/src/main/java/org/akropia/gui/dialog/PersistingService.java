package org.akropia.gui.dialog;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.akropia.diagnostics.DiagnosticEvent;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.gui.FileSelectingService;
import org.akropia.gui.event.model.GameController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.game.components.SavePreview;
import org.akropia.model.AkropiaException;
import org.akropia.model.Model;
import org.akropia.model.TurnPreparationException;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.model.campaign.GameModel;
import org.akropia.model.campaign.po.PersistingTool;
import org.akropia.model.campaign.po.RecoveryCheckpointPolicy;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.model.campaign.po.SaveBaseline;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames;
import org.akropia.model.campaign.po.TurnSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Perform load and save operations. When it's necessary it shows system file
 * dialog.
 */
@com.google.inject.Singleton
public class PersistingService {

    private final org.akropia.model.campaign.po.SavedGames savedGames;
    private final org.akropia.gui.util.ViewUtil view;
    private final org.akropia.i18n.I18n texts;
    private final org.akropia.gui.preferences.GamePreferences preferences;
    private final Logger logger = LoggerFactory
            .getLogger(PersistingService.class);

    private final FileSelectingService fileSelectingService;

    private final GameController gameController;

    private final PersistingTool persistingTool;

    private final EventBus eventBus;
    private final GameModelController gameModelController;

    private final DiagnosticLog diagnosticLog;

    private boolean busy;

    /**
     * The last successfully saved or loaded gameplay state with its file, or
     * <code>null</code> when the active game was never saved (R-0387).
     */
    private volatile SaveBaseline baseline;

    /** Model and turn of the last turn snapshot, so a repeat writes nothing. */
    private Model snapshotModel;

    private int snapshotTurn;

    private final Consumer<Runnable> fxThread;

    @Inject
    public PersistingService(final GameController gameController,
            final EventBus eventBus, final PersistingTool persistingTool,
            final FileSelectingService fileSelectingService,
            final GameModelController gameModelController,
            final DiagnosticLog diagnosticLog,
            final org.akropia.model.campaign.po.SavedGames savedGames,
            final org.akropia.gui.util.ViewUtil view,
            final org.akropia.i18n.I18n texts,
            final org.akropia.gui.preferences.GamePreferences preferences) {
        this(gameController, eventBus, persistingTool, fileSelectingService,
                gameModelController, diagnosticLog, savedGames, view, texts,
                preferences, PersistingService::runOnFxThread);
    }

    /**
     * @param fxThread runs feedback of the model thread on the JavaFX thread
     */
    public PersistingService(final GameController gameController,
            final EventBus eventBus, final PersistingTool persistingTool,
            final FileSelectingService fileSelectingService,
            final GameModelController gameModelController,
            final DiagnosticLog diagnosticLog,
            final org.akropia.model.campaign.po.SavedGames savedGames,
            final org.akropia.gui.util.ViewUtil view,
            final org.akropia.i18n.I18n texts,
            final org.akropia.gui.preferences.GamePreferences preferences,
            final Consumer<Runnable> fxThread) {
        this.fxThread = Preconditions.checkNotNull(fxThread);
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

    /**
     * Export the game to a file the player chooses.
     *
     * @return <code>true</code> only when the chosen file was written
     */
    public boolean saveModel(final Model model) {
        final File saveFile;
        // Export may run inside a load that is already busy.
        final boolean wasBusy = busy;
        busy = true;
        try {
            saveFile = fileSelectingService.saveFile(
                    persistingTool.getRootSaveDirectory(),
                    persistingTool.getSuggestedSaveFileName(model));
        } catch (AkropiaException unavailable) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, unavailable);
            gameModelController.showRecoverySaveFailure();
            return false;
        } finally {
            busy = wasBusy;
        }
        if (saveFile == null) {
            logger.debug("User didn't select any file to save game");
            return false;
        }
        return saveModelToFile(saveFile);
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
        } catch (AkropiaException unavailable) {
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

    private boolean saveModelToFile(final File targetFile) {
        final File file = correctFileNameExtension(targetFile);
        logger.debug("Saving game to file '{}' ", file.getAbsolutePath());
        return writeModelToFile(file, SavedGames.Kind.MANUAL);
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
            // The validated and started game is the state its file holds.
            remember(file, this::currentState);
        }
        return loaded;
    }

    @FunctionalInterface
    private interface SavedState {
        JsonElement get();
    }

    private JsonElement currentState() {
        return gameController.gameplayState();
    }

    /**
     * Make the state the baseline; without a usable state the game counts as
     * never saved.
     */
    private void remember(final File file, final SavedState state) {
        try {
            baseline = SaveBaseline.of(file, state.get());
        } catch (IllegalStateException | IllegalArgumentException unavailable) {
            // Includes TurnPreparationException of an incomplete turn.
            baseline = null;
        }
    }

    /**
     * @return whether leaving now loses no gameplay: the current state is the
     *         one the last successful save or load left in its unchanged file
     */
    public boolean isProgressSaved() {
        final SaveBaseline saved = baseline;
        if (saved == null || !gameModelController.isHumanTurnReady()) {
            return false;
        }
        try {
            return saved.covers(currentState());
        } catch (IllegalStateException incomplete) {
            // An incomplete turn (TurnPreparationException) is never safely
            // saved.
            return false;
        }
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
                throw new AkropiaException(String.format(
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
            throw new AkropiaException(String
                    .format("Unable to get file name from path '%s' ", path));
        } else {
            return fileName.toString();
        }
    }

    private boolean writeModelToFile(final File targetFile,
            final SavedGames.Kind kind) {
        try {
            final JsonElement state = gameController
                    .writeModelToFile(targetFile, kind);
            remember(targetFile, () -> state);
            preferences.setGameInProgressSaveFile(targetFile.getAbsolutePath());
            feedback(SavedGameText.saved);
            return true;
        } catch (TurnPreparationException rejected) {
            diagnosticLog.record(DiagnosticEvent.TURN_PREPARATION_REJECTED,
                    rejected);
            gameModelController.showTurnPreparationUnavailable();
        } catch (AkropiaException failed) {
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
        saveThroughBrowser(DiagnosticEvent.SAVE_FAILED);
    }

    /**
     * Open the manual Save browser for the current game. A failed action
     * reports itself and keeps the browser for another choice.
     *
     * @param failure diagnostic event of an unusable save folder
     * @return <code>true</code> only when the player completed a save that
     *         succeeded; Cancel, Escape, closing the browser or a failure
     *         return <code>false</code>
     */
    private boolean saveThroughBrowser(final DiagnosticEvent failure) {
        if (!gameModelController.isHumanTurnReady()) {
            return false;
        }
        // The detail shows the state a new save would store.
        final var game = gameModelController.getGameModel();
        final SaveSummaryPo current = game.getSaveSummary();
        final byte[] currentImage = SavePreview.render(game.getModel());
        final String suggestedName = suggestedName(current);
        while (true) {
            final List<SavedGames.Entry> entries;
            try {
                entries = savedGames.list();
            } catch (AkropiaException unavailable) {
                diagnosticLog.record(failure, unavailable);
                gameModelController.showSaveFolderUnavailable();
                return false;
            }
            final SavedGamesDialog.Choice choice = new SavedGamesDialog(view,
                    texts).chooseSave(entries, this::preview, current,
                            currentImage, suggestedName);
            if (choice == null) {
                return false;
            }
            if (perform(choice)) {
                return true;
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
            out += " " + texts.get(org.akropia.gui.Loc
                    .getTerrainDescription(org.akropia.model.Calendar.Season
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
        } catch (AkropiaException failed) {
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
                return saveModel(gameModelController.getModel());
            case FOLDER:
                java.awt.Desktop.getDesktop()
                        .open(persistingTool.getRootSaveDirectory());
                return false;
            default:
                throw new IllegalStateException("Unknown saved-game action");
            }
        } catch (java.io.IOException | UnsupportedOperationException
                | SecurityException | AkropiaException failed) {
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
            final JsonElement state = gameController.writeNamedGame(file, name,
                    kind);
            remember(file, () -> state);
            preferences.setGameInProgressSaveFile(file.getAbsolutePath());
            feedback(SavedGameText.saved);
            return true;
        } catch (TurnPreparationException rejected) {
            gameModelController.showTurnPreparationUnavailable();
        } catch (AkropiaException failed) {
            diagnosticLog.record(DiagnosticEvent.SAVE_FAILED, failed);
            gameModelController.showRecoverySaveFailure();
        } catch (IllegalArgumentException invalidName) {
            final DialogMessage message = new DialogMessage(view, texts);
            message.setText(texts.get(SavedGameText.invalidName));
            message.showAndWait();
        }
        return false;
    }

    /**
     * The one leave rule of menu, quit, window close, restart and load: a
     * safely saved game is left at once, otherwise the player chooses to save,
     * discard or stay (R-0387).
     *
     * @return <code>true</code> when the game may be replaced or left
     */
    public boolean confirmLeave() {
        if (!gameModelController.isGameModelReady()
                || gameModelController.getModel().isGameFinished()
                || isProgressSaved()) {
            return true;
        }
        // A game never saved, e.g. a new world without autosave, says so.
        final int choice = new SaveConfirmation(view, texts)
                .choose(texts.get(baseline == null ? SavedGameText.leaveNew
                        : SavedGameText.leave), true);
        return leaveAfter(choice, this::saveBeforeLeaving);
    }

    /**
     * @param choice 2 leaves without saving, 1 saves first, anything else stays
     * @param save   saves the game, telling whether a save succeeded
     * @return whether the pending leave action may continue
     */
    static boolean leaveAfter(final int choice, final BooleanSupplier save) {
        return choice == 2 || choice == 1 && save.getAsBoolean();
    }

    /**
     * Save and continue opens the regular manual Save browser, where the player
     * names a new save or confirms an overwrite (R-0387 user correction). No
     * recovery file is written instead.
     */
    private boolean saveBeforeLeaving() {
        // An incomplete turn cannot be saved; the game stays and no Save
        // browser opens.
        if (!gameModelController.isHumanTurnReady()) {
            gameModelController.showTurnPreparationUnavailable();
            return false;
        }
        try {
            currentState();
        } catch (TurnPreparationException rejected) {
            diagnosticLog.record(DiagnosticEvent.TURN_PREPARATION_REJECTED,
                    rejected);
            gameModelController.showTurnPreparationUnavailable();
            return false;
        }
        return saveThroughBrowser(DiagnosticEvent.EXIT_SAVE_FAILED);
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
        } catch (AkropiaException failed) {
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
        } catch (AkropiaException failed) {
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
            checkpointNewGame();
        }
    }

    /**
     * A new game starts unsaved; with autosave its ready start becomes its
     * first turn snapshot (R-0387, R-0388).
     */
    public void checkpointNewGame() {
        baseline = null;
        if (!gameModelController.isGameModelReady()) {
            return;
        }
        checkpoint(gameModelController.getModel(),
                RecoveryCheckpointPolicy.slotsForNewGame(
                        gameModelController.isHumanTurnReady(),
                        preferences.isAutosaveEnabled()));
    }

    /**
     * Save a prepared human turn as the next turn snapshot of its playthrough
     * and copy it into the selected recovery slots (R-0388). The new snapshot
     * is complete before Continue, the baseline or cleanup change; a repeated
     * notification of the same boundary writes nothing. Called on the model
     * thread.
     *
     * @param model required model whose turn is ready
     * @param slots required recovery slots; none means no snapshot
     */
    public synchronized void checkpoint(final Model model,
            final List<RecoverySlot> slots) {
        Preconditions.checkNotNull(model);
        if (Preconditions.checkNotNull(slots).isEmpty()) {
            return;
        }
        final GameModel game = gameModelController.getGameModel();
        final int turn = model.getCalendar().getNumberOfPlayedTurns();
        if (game == null || game.getModel() != model
                || model == snapshotModel && turn == snapshotTurn) {
            return;
        }
        final TurnSnapshot snapshot;
        try {
            snapshot = gameController.writeTurnSnapshot(savedGames,
                    persistingTool, slots);
        } catch (TurnPreparationException rejected) {
            diagnosticLog.record(DiagnosticEvent.TURN_PREPARATION_REJECTED,
                    rejected);
            onFxThread(gameModelController::showTurnPreparationUnavailable);
            return;
        } catch (AkropiaException failedWrite) {
            // History, Continue and the unsaved status stay as they were.
            diagnosticLog.record(DiagnosticEvent.RECOVERY_SAVE_FAILED,
                    failedWrite);
            onFxThread(gameModelController::showRecoverySaveFailure);
            return;
        }
        snapshotModel = model;
        snapshotTurn = turn;
        remember(snapshot.getFile(), snapshot::getState);
        preferences.setGameInProgressSaveFile(
                snapshot.getFile().getAbsolutePath());
        if (snapshot.getSlotFailure().isPresent()) {
            diagnosticLog.record(DiagnosticEvent.RECOVERY_SAVE_FAILED,
                    snapshot.getSlotFailure().get());
            onFxThread(gameModelController::showRecoverySaveFailure);
            return;
        }
        final String saved = texts.get(SavedGameText.autosaved,
                model.getCalendar().getCurrentYear());
        // Extra snapshots stay until a later successful autosave removes them.
        final String message = snapshot.isPruned() ? saved
                : saved + " " + texts.get(SavedGameText.autosaveCleanup);
        onFxThread(() -> feedback(message));
    }

    private void onFxThread(final Runnable action) {
        fxThread.accept(action);
    }

    private static void runOnFxThread(final Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void feedback(final SavedGameText text) {
        feedback(texts.get(text));
    }

    private void feedback(final String text) {
        eventBus.post(
                new org.akropia.gui.screen.game.components.StatusBarMessageEvent(
                        text,
                        org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source.GAME));
    }
}
