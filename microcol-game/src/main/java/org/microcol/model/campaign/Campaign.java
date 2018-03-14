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
    List<Mission> getMissions();

    Mission getMisssionByName(String name);

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
    boolean isMissionEnabled(Mission mission);

}