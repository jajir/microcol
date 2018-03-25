package org.microcol.model.campaign;

import java.util.Map;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

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

    void setFinished(boolean isFinished);

    void startMission(Model model, MissionCallBack missionCallBack);

    Map<String, String> saveToMap();

    void initialize(ModelPo modelPo);

}