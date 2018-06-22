package org.microcol.gui.event.model;

import java.io.File;

import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.Mission;
import org.microcol.model.campaign.ModelCampaignDao;
import org.microcol.model.campaign.ModelMission;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Responsible for loading and storing game. Class knows about state of default
 * campaign.
 */
public class GameController {

    private final GameModelController gameModelController;

    private final BeforeGameStartController beforeGameStartController;

    private final CampaignManager campaignManager;

    private final ModelCampaignDao modelCampaignDao;

    private final MissionCallBack missionCallBack;

    @Inject
    GameController(final GameModelController gameModelController,
            final ModelCampaignDao modelCampaignDao,
            final CampaignManager campaignManager,
            final BeforeGameStartController beforeGameStartController,
            final MissionCallBack missionCallBack) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.modelCampaignDao = Preconditions.checkNotNull(modelCampaignDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.beforeGameStartController = Preconditions.checkNotNull(beforeGameStartController);
        this.missionCallBack = Preconditions.checkNotNull(missionCallBack);
    }

    /**
     * Allows to start testing scenario. In production mode it's blocked.
     *
     * @param fileName
     *            required class path related file name
     */
    public void startTestScenario(final String fileName) {
        startMission(modelCampaignDao.loadFromClassPath(fileName));
    }

    public void writeModelToFile(final File targetFile) {
        modelCampaignDao.saveToFile(targetFile.getAbsolutePath(),
                gameModelController.getModelCampaign());
    }

    public void startModelFromFile(final File sourceFile) {
        startMission(modelCampaignDao.loadFromFile(sourceFile.getAbsolutePath()));
    }
    
    //TODO don't use mission name as string use enum or constant.
    public void startCampaignMission(final CampaignName campaignName, final String missionName) {
        final Campaign campaign = campaignManager.getCampaignByName(campaignName);
        final Mission mission = campaign.getMisssionByName(missionName);
        startMission(modelCampaignDao.loadFromClassPath(mission.getModelFileName()));
    }

    private void startMission(final ModelMission modelMission) {
        beforeGameStartController.fireEvent(new BeforeGameStartEvent());
        gameModelController.setAndStartModel(modelMission, missionCallBack);
    }

    public boolean isDefaultCampaignFinished() {
        final Campaign campaign = campaignManager.getCampaignByName(CampaignNames.defaultCampaign);
        return campaign.isFinished();
    }

    public void stopGame() {
        gameModelController.stop();
    }
}
