package org.microcol.model.campaign;

import org.microcol.gui.MicroColException;
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
                .getCampaignByName(resolve(modelPo.getCampaign().getName()));
        final CampaignMission campaignMission = campaign
                .getMisssionByName(modelPo.getCampaign().getMission());
        final Mission<?> mission = campaignMission.makeMission();
        mission.initialize(modelPo);
        return new ModelMission(campaign, campaignMission, (AbstractMission<?>) mission,
                Model.make(modelPo, mission.getGameOverEvaluators()));
    }

    private CampaignName resolve(final String name) {
        if (CampaignNames.defaultCampaign.toString().equals(name)) {
            return CampaignNames.defaultCampaign;
        } else if (CampaignNames.freePlay.toString().equals(name)) {
            return CampaignNames.freePlay;
        }
        throw new MicroColException(String.format("Unabble to resolve campaign '%s'", name));
    }

    public void saveToFile(final String fileName, final ModelMission modelCampaign) {
        modelDao.saveToFile(fileName, modelCampaign.getModelPo());
    }

}
