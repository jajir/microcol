package org.akropia.gui.event.model;

import java.io.File;
import java.util.List;

import org.akropia.diagnostics.DiagnosticEvent;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.AkropiaException;
import org.akropia.model.game.GameModel;
import org.akropia.model.game.GameModelDao;
import org.akropia.model.game.FreePlayMap;
import org.akropia.model.Player;
import org.akropia.model.game.po.GameModelPo;
import org.akropia.model.game.po.SavedGames;
import org.akropia.model.game.po.TurnSnapshot;
import org.akropia.model.generation.GeneratedWorldSettings;
import org.akropia.gui.screen.game.components.SavePreview;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Responsible for loading and storing game. The model owns game rules.
 */
public final class GameController {

    private final GameModelController gameModelController;

    private final GameModelDao gameModelDao;

    private final EventBus eventBus;

    private final DiagnosticLog diagnosticLog;

    @Inject
    GameController(final GameModelController gameModelController,
            final GameModelDao gameModelDao, final EventBus eventBus,
            final DiagnosticLog diagnosticLog) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gameModelDao = Preconditions.checkNotNull(gameModelDao);

        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.diagnosticLog = Preconditions.checkNotNull(diagnosticLog);
    }

    /**
     * Save the current game with its summary and map preview.
     *
     * @param targetFile required target
     * @param kind       required kind of the save
     * @return the gameplay state the written file holds
     */
    public JsonElement writeModelToFile(final File targetFile,
            final SavedGames.Kind kind) {
        final GameModel game = gameModelController.getGameModel();
        return gameModelDao.saveGame(targetFile, game, kind, null,
                SavePreview.render(game.getModel()));
    }

    public JsonElement writeNamedGame(final File file, final String name) {
        return writeNamedGame(file, name, SavedGames.Kind.MANUAL);
    }

    /**
     * @return the gameplay state the written file holds
     */
    public JsonElement writeNamedGame(final File file, final String name,
            final SavedGames.Kind kind) {
        final GameModel game = gameModelController.getGameModel();
        // The name is validated before the preview is drawn or a file written.
        Player.normalizeDisplayName(name);
        return gameModelDao.saveGame(file, game, kind, name,
                SavePreview.render(game.getModel()));
    }

    /**
     * @return the gameplay state of the active game as a save would store it
     * @throws org.akropia.model.TurnPreparationException while the turn is not
     *                                                    complete
     */
    public JsonElement gameplayState() {
        return gameModelDao.gameplayState(gameModelController.getGameModel());
    }

    /**
     * Save the prepared turn of the active game as its next turn snapshot with
     * its map preview.
     *
     * @return the written snapshot
     */
    public TurnSnapshot writeTurnSnapshot(final SavedGames saves) {
        final GameModel game = gameModelController.getGameModel();
        return gameModelDao.saveTurnSnapshot(game, saves,
                SavePreview.render(game.getModel()));
    }

    public boolean loadModelFromFile(final File sourceFile) {
        return loadModelFromFile(sourceFile, () -> true);
    }

    public boolean loadModelFromFile(final File sourceFile,
            final java.util.function.BooleanSupplier beforeReplace) {
        Preconditions.checkNotNull(sourceFile, "Save file is null");
        final GameModel loaded;
        try {
            loaded = gameModelDao.loadFromFile(sourceFile, eventBus);
        } catch (IllegalArgumentException | IllegalStateException
                | AkropiaException | JsonParseException rejected) {
            diagnosticLog.record(DiagnosticEvent.LOAD_FAILED, rejected);
            gameModelController.showInvalidSavedGame();
            return false;
        }
        if (!beforeReplace.getAsBoolean()) {
            return false;
        }
        startGame(loaded);
        return true;
    }

    public void startFreePlay(final FreePlayMap map, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation) {
        startFreePlay(map, playerName, difficulty, nation,
                java.util.List.of(org.akropia.model.FreePlayGoal.INDEPENDENCE));
    }

    public void startFreePlay(final FreePlayMap map, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation,
            final java.util.List<org.akropia.model.FreePlayGoal> goals) {
        startGame(gameModelDao.createFreePlay(map, playerName, difficulty,
                nation, eventBus, goals));
    }

    /**
     * Start a generated New World. The active game is replaced only after the
     * whole new world exists.
     *
     * @return {@code true} when the generated world is now the active game
     */
    public boolean startGeneratedFreePlay(final GeneratedWorldSettings settings,
            final String playerName, final Difficulty difficulty,
            final EuropeanNation nation) {
        final GameModel generated;
        try {
            generated = gameModelDao.createGeneratedFreePlay(settings,
                    playerName, difficulty, nation, eventBus);
        } catch (IllegalArgumentException | IllegalStateException
                | AkropiaException rejected) {
            return false;
        }
        startGame(generated);
        return true;
    }

    public void startGeneratedFreePlay(
            final org.akropia.model.store.ModelPo world,
            final String playerName) {
        startGame(gameModelDao.createGeneratedFreePlay(world, playerName,
                eventBus));
    }

    /**
     * Start a new game from an authored scenario. The active game is replaced
     * only after the new one exists.
     *
     * @param scenario required authored scenario
     * @throws org.akropia.model.generation.ScenarioDraftException when the game
     *                                                             cannot start
     *                                                             it
     */
    public void startAuthoredScenario(final GameModelPo scenario) {
        startGame(gameModelDao.createAuthoredScenario(scenario, eventBus));
    }

    private void startGame(final GameModel gameMode) {
        gameModelController.setAndStartModel(gameMode);
    }

    public void stopGame() {
        gameModelController.stop();
    }
}
