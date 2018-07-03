package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Holds parameters needed for creation of mission. It simplify parameters
 * passing.
 */
public class MissionCreationContext {

	private final MissionCallBack missionCallBack;

	private final Model model;

	private final ModelPo modelPo;

	private final CampaignManager campaignManager;

	public MissionCreationContext(final MissionCallBack missionCallBack, final Model model, final ModelPo modelPo,
			final CampaignManager campaignManager) {
		this.missionCallBack = Preconditions.checkNotNull(missionCallBack);
		this.model = Preconditions.checkNotNull(model);
		this.modelPo = Preconditions.checkNotNull(modelPo);
		this.campaignManager = Preconditions.checkNotNull(campaignManager);
	}

	public MissionCallBack getMissionCallBack() {
		return missionCallBack;
	}

	public Model getModel() {
		return model;
	}

	public ModelPo getModelPo() {
		return modelPo;
	}

	public CampaignManager getCampaignManager() {
		return campaignManager;
	}

}
