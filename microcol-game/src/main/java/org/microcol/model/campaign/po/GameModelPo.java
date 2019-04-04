package org.microcol.model.campaign.po;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.store.ModelPo;

public class GameModelPo {

    private String campaignName;

    private String missionName;

    private Map<String, String> goalData;

    private ModelPo model;

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public ModelPo getModel() {
        return model;
    }

    public void setModel(ModelPo model) {
        this.model = model;
    }

    public Map<String, String> getGoalData() {
        if (goalData == null) {
            goalData = new HashMap<>();
        }
        return goalData;
    }

    public void setData(Map<String, String> data) {
        this.goalData = data;
    }

}
