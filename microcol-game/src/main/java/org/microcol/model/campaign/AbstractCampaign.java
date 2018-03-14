package org.microcol.model.campaign;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class AbstractCampaign implements Campaign {

    /**
     * Name is unique identification of campaign. It's later used as part of
     * localization key.
     */
    private final String name;

    private final List<Mission> missions = new ArrayList<>();

    AbstractCampaign(final String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    @Override
    public Mission getMisssionByName(final String name) {
        Preconditions.checkNotNull(name);
        final Optional<Mission> oMission = missions.stream()
                .filter(mission -> mission.getName().equals(name)).findAny();
        if (oMission.isPresent()) {
            return oMission.get();
        } else {
            throw new IllegalArgumentException(
                    String.format("There is no campaign for name '%s'", name));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Campaign#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Campaign#getMissions()
     */
    @Override
    public List<Mission> getMissions() {
        return missions.stream().sorted(Comparator.comparing(Mission::getOrderNo))
                .collect(ImmutableList.toImmutableList());
    }

    void addMission(final Mission mission) {
        Preconditions.checkNotNull(mission, "Mission is null.");
        missions.add(mission);
    }

    @Override
    public boolean isFinished() {
        return !missions.stream().filter(mission -> !mission.isFinished()).findAny().isPresent();
    }

    @Override
    public boolean isMissionEnabled(final Mission mission) {
        Preconditions.checkNotNull(mission, "Mission is null");
        boolean isPreviousFinished = false;
        for (final Mission m : getMissions()) {
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

}
