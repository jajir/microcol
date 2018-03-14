package org.microcol.model.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

public abstract class AbstractCampaign implements Campaign {

    /**
     * Name is unique identification of campaign. It's later used as part of
     * localization key.
     */
    private final String name;

    private final List<AbstractMission> missions = new ArrayList<>();

    AbstractCampaign(final String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    @Override
    public Mission getMisssionByName(final String name) {
        Preconditions.checkNotNull(name);
        final Optional<AbstractMission> oMission = missions.stream()
                .filter(mission -> mission.getName().equals(name)).findAny();
        if (oMission.isPresent()) {
            return oMission.get();
        } else {
            throw new IllegalArgumentException(
                    String.format("There is no campaign for name '%s'", name));
        }
    }

    /* (non-Javadoc)
     * @see org.microcol.model.campaign.Campaign#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.microcol.model.campaign.Campaign#getMissions()
     */
    @Override
    public List<AbstractMission> getMissions() {
        return missions;
    }

}
