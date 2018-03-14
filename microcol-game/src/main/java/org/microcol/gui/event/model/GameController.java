package org.microcol.gui.event.model;

import java.io.File;

import org.microcol.gui.util.PersistentService;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignDefault;
import org.microcol.model.campaign.CampaignManager;
import org.microcol.model.campaign.Mission;
import org.microcol.model.campaign.ModelCampaignDao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * responsible for loading and storing game.
 */
public class GameController {

    private final GameModelController gameModelController;

    private final PersistentService persistentService;

    private final CampaignManager campaignManager;

    private final ModelCampaignDao modelCampaignDao;

    @Inject
    GameController(final GameModelController gameModelController,
            final PersistentService persistentService, final ModelCampaignDao modelCampaignDao,
            final CampaignManager campaignManager) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.persistentService = Preconditions.checkNotNull(persistentService);
        this.modelCampaignDao = Preconditions.checkNotNull(modelCampaignDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
    }

    /**
     * Allows to start testing scenario. In production mode it's blocked.
     *
     * @param fileName
     *            required class path related file name
     */
    public void startTestScenario(final String fileName) {
        gameModelController.setAndStartModel(modelCampaignDao.loadFromClassPath(fileName));
    }

    public void startNewDefaultGame() {
        gameModelController.setAndStartModel(modelCampaignDao
                .loadFromClassPath(persistentService.getDefaultScenario().getFileName()));
    }

    public void writeModelToFile(final File targetFile) {
        modelCampaignDao.saveToFile(targetFile.getAbsolutePath(),
                gameModelController.getModelCampaign());
    }

    public void loadModelFromFile(final File sourceFile) {
        gameModelController
                .setAndStartModel(modelCampaignDao.loadFromFile(sourceFile.getAbsolutePath()));
    }

    public void startDefaultMission(final String missionName) {
        final Campaign campaign = campaignManager.getCampaignByName(CampaignDefault.NAME);
        final Mission mission = campaign.getMisssionByName(missionName);
        gameModelController
                .setAndStartModel(modelCampaignDao.loadFromClassPath(mission.getModelFileName()));
    }

}
