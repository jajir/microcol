package org.microcol.model.campaign.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base campaign persistent object. Holds info if campaign was finished of not.
 * Closer informations about particular game should be stored in game model.
 */
public class CampaignPo {

    private String name;

    private List<CampaignMissionPo> missions;

    public Optional<CampaignMissionPo> getMissionByName(final String missionName) {
        return getMissions().stream().filter(mission -> missionName.equals(mission.getName()))
                .findFirst();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CampaignMissionPo> getMissions() {
        if (missions == null) {
            missions = new ArrayList<>();
        }
        return missions;
    }

    public void setMissions(List<CampaignMissionPo> missions) {
        this.missions = missions;
    }

}
