package org.microcol.gui.event.model;

import java.io.File;
import java.util.List;

import org.microcol.diagnostics.DiagnosticEvent;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.MicroColException;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignMission;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.GameModel;
import org.microcol.model.campaign.GameModelDao;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.MissionName;
import org.microcol.model.Player;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.model.campaign.po.SavedGames;
import org.microcol.model.campaign.po.TurnSnapshot;
import org.microcol.model.generation.GeneratedWorldSettings;
import org.microcol.gui.screen.game.components.SavePreview;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Responsible for loading and storing game. Class knows about state of default
 * campaign.
 */
public final class GameController {

    private final GameModelController gameModelController;

    private final CampaignManager campaignManager;

    private final GameModelDao gameModelDao;

    private final EventBus eventBus;

    private final DiagnosticLog diagnosticLog;

    @Inject
    GameController(final GameModelController gameModelController,
            final GameModelDao gameModelDao,
            final CampaignManager campaignManager, final EventBus eventBus,
            final DiagnosticLog diagnosticLog) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gameModelDao = Preconditions.checkNotNull(gameModelDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
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
     * @throws org.microcol.model.TurnPreparationException while the turn is not
     *                                                     complete
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
    public TurnSnapshot writeTurnSnapshot(final SavedGames saves,
            final PersistingTool paths, final List<RecoverySlot> slots) {
        final GameModel game = gameModelController.getGameModel();
        return gameModelDao.saveTurnSnapshot(game, saves, paths, slots,
                SavePreview.render(game.getModel()));
    }

    public boolean restartMission(
            final java.util.function.BooleanSupplier beforeReplace) {
        final GameModel current = gameModelController.getGameModel();
        Preconditions.checkState(current.isCampaignMission());
        final GameModel fresh = gameModelDao
                .loadFromClassPath(current.getScenarioResource(), eventBus);
        if (!beforeReplace.getAsBoolean()) {
            return false;
        }
        startMission(fresh);
        return true;
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
                | MicroColException | JsonParseException rejected) {
            diagnosticLog.record(DiagnosticEvent.LOAD_FAILED, rejected);
            gameModelController.showInvalidSavedGame();
            return false;
        }
        if (!beforeReplace.getAsBoolean()) {
            return false;
        }
        startMission(loaded);
        return true;
    }

    public void startFreePlay(final FreePlayMap map, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation) {
        startMission(gameModelDao.createFreePlay(map, playerName, difficulty,
                nation, eventBus));
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
                | MicroColException rejected) {
            return false;
        }
        startMission(generated);
        return true;
    }

    public void startCampaignMission(final CampaignName campaignName,
            final MissionName missionName) {
        final Campaign campaign = campaignManager
                .getCampaignByName(campaignName);
        startCampaignMission(campaign.getMisssionByMissionName(missionName));
    }

    private void startCampaignMission(final CampaignMission campaignMission) {
        startMission(gameModelDao.loadFromClassPath(
                campaignMission.getClassPathFile(), eventBus));
    }

    private void startMission(final GameModel gameMode) {
        gameModelController.setAndStartModel(gameMode);
    }

    public boolean isDefaultCampaignFinished() {
        final Campaign campaign = campaignManager
                .getCampaignByName(CampaignNames.defaultCampaign);
        return campaign.isFinished();
    }

    public void stopGame() {
        gameModelController.stop();
    }
}
