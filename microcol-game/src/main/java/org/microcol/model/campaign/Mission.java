package org.microcol.model.campaign;

public interface Mission {

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the orderNo
     */
    Integer getOrderNo();

    /**
     * @return the modelFileName
     */
    String getModelFileName();

    /**
     * Inform if player finished this mission.
     *
     * @return return <code>true</code> when user already finished this mission
     *         otherwise return <code>false</code>.
     */
    boolean isFinished();

}