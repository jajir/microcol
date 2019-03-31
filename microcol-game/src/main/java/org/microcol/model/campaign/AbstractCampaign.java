package org.microcol.model.campaign;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.microcol.model.campaign.store.CampaignMissionPo;
import org.microcol.model.campaign.store.CampaignPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

abstract class AbstractCampaign implements Campaign {

    /**
     * Name is unique identification of campaign. It's later used as part of
     * localization key.
     */
    private final CampaignNames name;

    private final List<CampaignMission> missions = new ArrayList<>();

    AbstractCampaign(final CampaignNames name) {
        this.name = Preconditions.checkNotNull(name);
    }

    @Override
    public CampaignMission getMisssionByName(final String name) {
        Preconditions.checkNotNull(name);
        final Optional<CampaignMission> oMission = missions.stream()
                .filter(mission -> mission.getName().equals(name)).findAny();
        return oMission.orElseThrow(() -> new IllegalArgumentException(
                String.format("There is no campaign for name '%s'", name)));
    }

    @Override
    public CampaignMission getMisssionByMissionName(final MissionName missionName) {
        Preconditions.checkNotNull(missionName);
        final Optional<CampaignMission> oMission = missions.stream()
                .filter(mission -> mission.getMissionName().equals(missionName)).findAny();
        return oMission.orElseThrow(() -> new IllegalArgumentException(
                String.format("There is no mission for missio name '%s'", missionName)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(AbstractCampaign.class).add("name", name)
                .add("missions", missions).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Campaign#getName()
     */
    @SuppressWarnings("unchecked")
    @Override
    public CampaignNames getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Campaign#getMissions()
     */
    @Override
    public List<CampaignMission> getMissions() {
        return missions.stream().sorted(Comparator.comparing(CampaignMission::getOrderNo))
                .collect(ImmutableList.toImmutableList());
    }

    void addMission(final CampaignMission mission) {
        Preconditions.checkNotNull(mission, "Mission is null.");
        missions.add(mission);
    }

    @Override
    public boolean isFinished() {
        return !missions.stream().filter(mission -> !mission.isFinished()).findAny().isPresent();
    }

    @Override
    public boolean isMissionEnabled(final CampaignMission mission) {
        Preconditions.checkNotNull(mission, "Mission is null");
        boolean isPreviousFinished = true;
        for (final CampaignMission m : getMissions()) {
            if (m.equals(mission)) {
                if (m.isFinished()) {
                    return true;
                } else {
                    return isPreviousFinished;
                }
            }
            isPreviousFinished = m.isFinished();
        }
        throw new IllegalStateException(
                String.format("Campaign '%s' doesn't contains mission '%s'", name, mission));
    }

    @Override
    public CampaignPo save() {
        final CampaignPo out = new CampaignPo();
        out.setName(name.toString());
        missions.forEach(mission -> {
            final CampaignMissionPo m = new CampaignMissionPo();
            m.setName(mission.getName());
            m.setWasFinished(mission.isFinished());
            out.getMissions().add(m);
        });
        return out;
    }

    @Override
    public void load(final CampaignPo campaignPo) {
        missions.forEach(mission -> {
            final Optional<CampaignMissionPo> oPo = campaignPo.getMissionByName(mission.getName());
            if (oPo.isPresent()) {
                mission.setFinished(oPo.get().isWasFinished());
            } else {
                mission.setFinished(false);
            }
        });
    }

}
