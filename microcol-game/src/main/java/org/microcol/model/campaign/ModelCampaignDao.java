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

    public ModelMission loadFromFile(final String fileName) {
        return makeFromModelPo(modelDao.loadModelFromFile(fileName));
    }

    public ModelMission loadFromClassPath(final String fileName) {
        return makeFromModelPo(modelDao.loadPredefinedModel(fileName));
    }

    private ModelMission makeFromModelPo(final ModelPo modelPo) {
        final Campaign campaign = campaignManager
                .getCampaignByName(modelPo.getCampaign().getName());
        final Mission mission = campaign.getMisssionByName(modelPo.getCampaign().getMission());
        mission.initialize(modelPo);
        return new ModelMission(campaign, (AbstractMission) mission, Model.make(modelPo));
    }

    public void saveToFile(final String fileName, final ModelMission modelCampaign) {
        modelDao.saveToFile(fileName, modelCampaign.getModelPo());
    }

}
