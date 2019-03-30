package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.campaign.store.CampaignPo;
import org.microcol.model.campaign.store.CampaignsDao;
import org.microcol.model.campaign.store.CampaignsPo;

import com.google.common.base.Preconditions;

/**
 * Contains all campaigns objects. Allows to get campaign by it's name.
 */
public final class CampaignManager {

    private final Map<? extends CampaignName, Campaign> campaigns;

    private final CampaignsDao campaignsDao;

    CampaignManager(final List<Campaign> campaigns, final CampaignsDao campaignsDao) {
        this.campaignsDao = Preconditions.checkNotNull(campaignsDao);
        this.campaigns = Preconditions.checkNotNull(campaigns).stream()
                .collect(Collectors.toMap(c -> c.getName(), Function.identity()));
        loadMissionStateFromPreferences();
    }

    private void loadMissionStateFromPreferences() {
        final CampaignsPo campaignsPo = campaignsDao.load();
        campaigns.forEach((name, campaign) -> {
            final Optional<CampaignPo> oCampaignPo = campaignsPo
                    .getCampaignByName(campaign.getName().toString());
            campaign.load(oCampaignPo.orElse(new CampaignPo()));
        });
    }

    void saveMissionState() {
        final CampaignsPo campaignsPo = new CampaignsPo();
        campaigns.forEach((name, campaign) -> {
            campaignsPo.getCampaigns().add(campaign.save());
        });
        campaignsDao.save(campaignsPo);
    }

    /**
     * Get campaign by it's name.
     *
     * @param name
     *            required campaign name
     * @param <C>
     *            class providing campaign name
     * @return campaign object
     */
    public <C extends CampaignName> Campaign getCampaignByName(final C name) {
        Preconditions.checkNotNull(name, "Campaign name is null");
        final Campaign out = campaigns.get(name);
        if (out == null) {
            throw new IllegalArgumentException(
                    String.format("There is no campaign for name '%s'", name));
        }
        return out;
    }

    public Campaign getDefaultCampain() {
        return getCampaignByName(CampaignNames.defaultCampaign);
    }

}
