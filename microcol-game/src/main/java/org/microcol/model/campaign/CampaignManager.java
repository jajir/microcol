package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Contains all campaigns objects. Allows to get campaign by it's name.
 */
public class CampaignManager {

    private final Map<String, Campaign> campaigns;

    private final Preferences preferences = Preferences.userNodeForPackage(CampaignManager.class);

    CampaignManager(final List<Campaign> campaigns) {
        this.campaigns = Preconditions.checkNotNull(campaigns).stream()
                .collect(Collectors.toMap(c -> c.getName(), Function.identity()));
        setIsFinished();
    }

    private void setIsFinished() {
        campaigns.forEach((name, campaign) -> {
            campaign.getMissions().forEach(mission -> {
                final String key = campaign.getName() + "." + mission.getName() + ".isFinished";
                mission.setFinished(preferences.getBoolean(key, false));
            });
        });

    }

    /**
     * Get campaign by it's name.
     *
     * @param name
     *            required campaign name
     * @return campaign object
     */
    public Campaign getCampaignByName(final String name) {
        Preconditions.checkNotNull(name, "Campaign name is null");
        final Campaign out = campaigns.get(name);
        if (out == null) {
            throw new IllegalArgumentException(
                    String.format("There is no campaign for name '%s'", name));
        }
        return out;
    }

    public Campaign getDefaultCampain() {
        return getCampaignByName(CampaignDefault.NAME);
    }

}
