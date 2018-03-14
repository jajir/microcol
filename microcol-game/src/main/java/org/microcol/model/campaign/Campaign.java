package org.microcol.model.campaign;

import java.util.List;

/**
 * Campaign interface.
 */
public interface Campaign {

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the missions
     */
    List<AbstractMission> getMissions();

    Mission getMisssionByName(final String name);

}