package org.microcol.model.campaign.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class CampaignsPo {

    private List<CampaignPo> campaigns;

    public List<CampaignPo> getCampaigns() {
        if (campaigns == null) {
            campaigns = new ArrayList<>();
        }
        return campaigns;
    }

    public Optional<CampaignPo> getCampaignByName(final String campaignName) {
        Preconditions.checkNotNull(campaignName);
        return getCampaigns().stream().filter(campaign -> campaignName.equals(campaign.getName()))
                .findFirst();
    }

    public void setCampaigns(List<CampaignPo> campaigns) {
        this.campaigns = campaigns;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CampaignsPo.class).add("campaigns", campaigns).toString();
    }

}
