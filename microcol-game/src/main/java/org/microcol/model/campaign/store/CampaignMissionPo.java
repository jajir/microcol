package org.microcol.model.campaign.store;

import com.google.common.base.MoreObjects;

public class CampaignMissionPo {

    private String name;

    private boolean wasFinished;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CampaignMissionPo.class).add("name", name)
                .add("wasFinished", wasFinished).toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWasFinished() {
        return wasFinished;
    }

    public void setWasFinished(boolean wasFinished) {
        this.wasFinished = wasFinished;
    }

}
