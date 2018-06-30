package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

public interface Mission {

    /**
     * @return the modelFileName
     */
    String getModelFileName();

    /**
     * TODO it create circular dependency, remove it.
     *
     * @param campaignManager
     *            required campaign manager
     */
    void setCampaignManager(CampaignManager campaignManager);

    /**
     * Get mission goals.
     *
     * @return mission goals
     */
    MissionGoals getGoals();

    void startMission(Model model, MissionCallBack missionCallBack);

    Map<String, String> saveToMap();

    void initialize(ModelPo modelPo);

    /**
     * Get list of functions that evaluate game over conditions. When one
     * function return nun-null return than game is over and returned object is
     * passed in game over event to frontend.
     * 
     * @return list of game over evaluators
     */
    List<Function<Model, GameOverResult>> getGameOverEvaluators();

}