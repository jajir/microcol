package org.microcol.model.campaign;

import java.util.List;

import org.microcol.model.campaign.store.CampaignPo;

/**
 * Campaign interface.
 */
public interface Campaign {

    /**
     * @param <C>
     *            class providing campaign name
     *
     * @return the name
     */
    <C extends CampaignName> C getName();

    /**
     * @return the missions
     */
    List<CampaignMission> getMissions();

    CampaignMission getMisssionByName(String name);

    CampaignMission getMisssionByMissionName(MissionName missionName);

    /**
     * Inform if player finished this campaign. Player finish campaign when
     * finish all missions.
     *
     * @return return <code>true</code> when user already finished this campaign
     *         otherwise return <code>false</code>.
     */
    boolean isFinished();

    /**
     * Return if user could start given mission. Mission could be started when
     * was already finished or when is next after already finished mission.
     *
     * @param mission
     *            required mission that belongs to this campaign
     * @return return <code>true</code> when mission could be played by user
     *         otherwise return <code>false</code>.
     */
    boolean isMissionEnabled(CampaignMission mission);

    void load(CampaignPo campaignPo);

    CampaignPo save();

}