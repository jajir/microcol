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
public final class CampaignManager {

    private final Map<? extends CampaignName, Campaign> campaigns;

    private final Preferences preferences = Preferences.userNodeForPackage(CampaignManager.class);

    CampaignManager(final List<Campaign> campaigns) {
        this.campaigns = Preconditions.checkNotNull(campaigns).stream()
                .collect(Collectors.toMap(c -> c.getName(), Function.identity()));
        loadMissionStateFromPreferences();
    }

    private void loadMissionStateFromPreferences() {
        wentThroughMissions((key, mission) -> {
            mission.setFinished(preferences.getBoolean(key, false));
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

    private void wentThroughMissions(final BiConsumer<String, CampaignMission> consumer) {
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
