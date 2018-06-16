package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.microcol.gui.MicroColException;

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
        loadMissionStateFromPreferences();
    }

    private void loadMissionStateFromPreferences() {
        wentThroughMissions((key, mission) -> {
            mission.setFinished(preferences.getBoolean(key, false));
            mission.setCampaignManager(this);
        });
    }

    void saveMissionState() {
        wentThroughMissions((key, mission) -> {
            preferences.putBoolean(key, mission.isFinished());
            mission.setFinished(preferences.getBoolean(key, false));
        });
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    private void wentThroughMissions(BiConsumer<String, Mission> consumer) {
        campaigns.forEach((name, campaign) -> {
            campaign.getMissions().forEach(mission -> consumer
                    .accept(campaign.getName() + "." + mission.getName() + ".isFinished", mission));
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
        return getCampaignByName(Default_campaign.NAME);
    }

}
