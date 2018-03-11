package org.microcol.model.campaign;

import org.microcol.model.Model;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Load and save model campaign from to file.
 */
public class ModelCamapignDao {

    private final ModelDao modelDao;

    private final CampaignManager campaignManager;

    ModelCamapignDao(final ModelDao modelDao, final CampaignManager campaignManager) {
        this.modelDao = Preconditions.checkNotNull(modelDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
    }

    ModelCampaign load(final String fileName) {
        final ModelPo modelPo = modelDao.loadModelFromFile(fileName);
        final AbstractCampaign campaign = campaignManager
                .getCmapaignByName(modelPo.getCampaign().getName());
        final AbstractMission mission = campaign
                .getMisssionByName(modelPo.getCampaign().getMission());
        return new ModelCampaign(campaign, mission, Model.make(modelPo));
    }

    public void saveToFile(final String fileName, final ModelCampaign modelCampaign) {
        modelDao.saveToFile(fileName, modelCampaign.getModelPo());
    }

}
