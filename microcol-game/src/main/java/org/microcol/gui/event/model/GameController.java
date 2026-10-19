package org.microcol.gui.event.model;

import java.io.File;

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

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
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

    public void writeModelToFile(final File targetFile) {
        gameModelDao.saveToFile(targetFile.getAbsolutePath(),
                gameModelController.getGameModel());
    }

    public boolean loadModelFromFile(final File sourceFile) {
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
        startMission(loaded);
        return true;
    }

    public void startFreePlay(final FreePlayMap map, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation) {
        startMission(gameModelDao.createFreePlay(map, playerName, difficulty,
                nation, eventBus));
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
