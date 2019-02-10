package org.microcol.gui.event.model;

import java.io.File;

import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignMission;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.MissionName;
import org.microcol.model.campaign.ModelCampaignDao;
import org.microcol.model.campaign.ModelMission;

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

    private final ModelCampaignDao modelCampaignDao;

    private final MissionCallBack missionCallBack;

    private final EventBus eventBus;

    @Inject
    GameController(final GameModelController gameModelController,
            final ModelCampaignDao modelCampaignDao, final CampaignManager campaignManager,
            final MissionCallBack missionCallBack, final EventBus eventBus) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.modelCampaignDao = Preconditions.checkNotNull(modelCampaignDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.missionCallBack = Preconditions.checkNotNull(missionCallBack);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    /**
     * Allows to start testing scenario. In production mode it's blocked.
     *
     * @param fileName
     *            required class path related file name
     */
    public void startTestScenario(final String fileName) {
        startMission(modelCampaignDao.loadFromClassPath(fileName, missionCallBack));
    }

    public void writeModelToFile(final File targetFile) {
        modelCampaignDao.saveToFile(targetFile.getAbsolutePath(),
                gameModelController.getModelMission());
    }

    public void loadModelFromFile(final File sourceFile) {
        startMission(modelCampaignDao.loadFromFile(sourceFile.getAbsolutePath(), missionCallBack));
    }

    public void startCampaignMission(final CampaignName campaignName, final String missionName) {
        final Campaign campaign = campaignManager.getCampaignByName(campaignName);
        startCampaignMission(campaign.getMisssionByName(missionName));
    }

    public void startCampaignMission(final CampaignName campaignName,
            final MissionName missionName) {
        final Campaign campaign = campaignManager.getCampaignByName(campaignName);
        startCampaignMission(campaign.getMisssionByMissionName(missionName));
    }

    private void startCampaignMission(final CampaignMission campaignMission) {
        startMission(modelCampaignDao.loadFromClassPath(campaignMission.getClassPathFile(),
                missionCallBack));
    }

    private void startMission(final ModelMission modelMission) {
        eventBus.post(new BeforeGameStartEvent());
        gameModelController.setAndStartModel(modelMission);
    }

    public boolean isDefaultCampaignFinished() {
        final Campaign campaign = campaignManager.getCampaignByName(CampaignNames.defaultCampaign);
        return campaign.isFinished();
    }

    public void stopGame() {
        gameModelController.stop();
    }
}
