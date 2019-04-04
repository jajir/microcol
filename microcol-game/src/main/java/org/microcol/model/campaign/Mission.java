package org.microcol.model.campaign;

import java.util.Map;

interface Mission<G extends MissionGoals> {

    /**
     * Get mission goals.
     *
     * @return mission goals
     */
    G getGoals();

    /**
     * Save state of mission goals to map. Map could be easily stored in file.
     * 
     * @return map
     */
    Map<String, String> saveGoals();

}