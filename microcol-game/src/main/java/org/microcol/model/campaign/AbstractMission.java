package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Abstract mission. Contain some basic functionality.
 */
public abstract class AbstractMission<G extends MissionGoals, T extends MissionContext> implements Mission<G> {

    private final String name;

    /**
     * Class path related mission model file name. It's something like
     * <i>/maps/03-before-game-over.json</i>.
     */
    private final String modelFileName;

    private final Integer orderNo;

    private CampaignManager campaignManager;

    /**
     * Running mission context. There is stored player's achievements.
     */
    private T context;
    
    private final G goals;

    AbstractMission(final String name, final Integer orderNo, final String modelFileName, final G goals) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.modelFileName = Preconditions.checkNotNull(modelFileName);
        this.goals = Preconditions.checkNotNull(goals);
    }

    //TODO there is lot of dependencies to save mission result
    protected void setFinished(final boolean finished) {
        Preconditions.checkNotNull(campaignManager, "campaignManager is null");
        final Campaign campaign  = campaignManager.getCampaignByName(getCampaignKey());
        final CampaignMission campaignMission = campaign.getMisssionByName(name);
        campaignMission.setFinished(finished);
        campaignManager.saveMissionState();
    }
    
    abstract <C extends CampaignName> C getCampaignKey(); 
    
    @Override
    public G getGoals() {
        return goals;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getModelFileName()
     */
    @Override
    public String getModelFileName() {
        return modelFileName;
    }

    @Override
    public void setCampaignManager(final CampaignManager campaignManager) {
        this.campaignManager = campaignManager;
    }

    protected Integer get(final Map<String, String> map, final String key) {
        String val = map.get(key);
        if (val == null) {
            return 0;
        } else {
            return Integer.parseInt(val);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name).add("orderNo", orderNo)
                .add("modelFileName", modelFileName).toString();
    }

    // TODO mission game over evaluation should be at mission definition
    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    protected abstract T getNewContext();

    protected abstract GameOverResult evaluateGameOver(Model model);

    @Override
    public void initialize(final ModelPo modelPo) {
        Preconditions.checkNotNull(modelPo, "Model persisten obbject is null");
        context = getNewContext();
        context.initialize(modelPo);
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out =getContext().saveToMap();
        goals.save(out);
        return out;
    }

    /**
     * @return the context
     */
    protected T getContext() {
        return context;
    }

    /**
     * @param context
     *            the context to set
     */
    protected void setContext(final T context) {
        Preconditions.checkNotNull(context);
        this.context = context;
    }

}
