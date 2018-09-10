package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

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
     * Get mission name definition object.
     *
     * @return mission name object
     */
    MissionName getMissionName();

    /**
     * Get file at class path where is model definition file.
     *
     * @return mission definition file
     */
    String getClassPathFile();

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
     * 
     * @param missionCallBack
     *            required front-end call back
     * @param model
     *            required game model
     * @param modelPo
     *            required model persistent object
     * @param campaignManager
     *            required campaign manager
     * @return return mission instance
     */
    Mission<?> makeMission(final MissionCallBack missionCallBack, final Model model,
            final ModelPo modelPo, final CampaignManager campaignManager);

}
