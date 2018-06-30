package org.microcol.model.campaign;

/**
 * Holds campaign mission definition. Also works as abstract factory for
 * mission.
 */
public interface CampaignMission {

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the orderNo
     */
    Integer getOrderNo();

    /**
     * Inform if player finished this mission.
     *
     * @return return <code>true</code> when user already finished this mission
     *         otherwise return <code>false</code>.
     */
    boolean isFinished();

    /**
     * Allows to set if campaign mission is finished.
     *
     * @param isFinished
     *            if campaign is finished it's <code>true</code> otherwise it's
     *            <code>false</code>
     */
    void setFinished(boolean isFinished);

    /**
     * It's concrete mission factory.
     */
    Mission makeMission();

}
