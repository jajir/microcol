package org.microcol.model.campaign;

import org.microcol.model.Model;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Load and save model campaign from to file.
 */
public class ModelCampaignDao {

    private final ModelDao modelDao;

    private final CampaignManager campaignManager;

    @Inject
    ModelCampaignDao(final ModelDao modelDao, final CampaignManager campaignManager) {
        this.modelDao = Preconditions.checkNotNull(modelDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
    }

    public ModelCampaign loadFromFile(final String fileName) {
        return makeFromModelPo(modelDao.loadModelFromFile(fileName));
    }

    public ModelCampaign loadFromClassPath(final String fileName) {
        return makeFromModelPo(modelDao.loadPredefinedModel(fileName));
    }

    private ModelCampaign makeFromModelPo(final ModelPo modelPo) {
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
