package org.microcol.gui.event.model;

import java.io.File;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.CampaignMission;
import org.microcol.model.campaign.GameModel;
import org.microcol.model.campaign.GameModelDao;
import org.microcol.model.campaign.MissionName;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Responsible for loading and storing game. Class knows about state of default
 * campaign.
 */
public final class GameController {

    private final GameModelController gameModelController;

    private final CampaignManager campaignManager;

    private final GameModelDao gameModelDao;

    private final EventBus eventBus;

    @Inject
    GameController(final GameModelController gameModelController, final GameModelDao gameModelDao,
            final CampaignManager campaignManager, final EventBus eventBus) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gameModelDao = Preconditions.checkNotNull(gameModelDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    public void writeModelToFile(final File targetFile) {
        gameModelDao.saveToFile(targetFile.getAbsolutePath(), gameModelController.getGameModel());
    }

    public void loadModelFromFile(final File sourceFile) {
        startMission(gameModelDao.loadFromFile(sourceFile, eventBus));
    }

    public void startCampaignMission(final CampaignName campaignName,
            final MissionName missionName) {
        final Campaign campaign = campaignManager.getCampaignByName(campaignName);
        startCampaignMission(campaign.getMisssionByMissionName(missionName));
    }

    private void startCampaignMission(final CampaignMission campaignMission) {
        startMission(gameModelDao.loadFromClassPath(campaignMission.getClassPathFile(), eventBus));
    }

    private void startMission(final GameModel gameMode) {
        gameModelController.setAndStartModel(gameMode);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    public boolean isDefaultCampaignFinished() {
        final Campaign campaign = campaignManager.getCampaignByName(CampaignNames.defaultCampaign);
        return campaign.isFinished();
    }

    public void stopGame() {
        gameModelController.stop();
    }
}
