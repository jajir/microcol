package org.microcol.model.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

public abstract class AbstractCampaign {

    private final String name;

    private final List<AbstractMission> missions = new ArrayList<>();

    AbstractCampaign(final String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    AbstractMission getMisssionByName(final String name) {
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the missions
     */
    public List<AbstractMission> getMissions() {
        return missions;
    }

}
