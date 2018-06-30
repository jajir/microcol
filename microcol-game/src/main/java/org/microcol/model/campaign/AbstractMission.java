package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.gui.MicroColException;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Abstract mission. Contain some basic functionality.
 */
public abstract class AbstractMission<T extends MissionContext> implements Mission {

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

    AbstractMission(final String name, final Integer orderNo, final String modelFileName) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.modelFileName = Preconditions.checkNotNull(modelFileName);
    }

    protected void setFinished(final boolean finished) {
        // FIXME store it?
    }
    
    @Override
    public MissionGoals getGoals() {
        // FIXME Auto-generated method stub
        return null;
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

    void flush() {
        Preconditions.checkNotNull(campaignManager, "campaignManager is null");
        campaignManager.saveMissionState();
    }

    protected boolean isFirstTurn(final Model model) {
        return model.getCalendar().getCurrentYear() == model.getCalendar().getStartYear();
    }

    protected Player getHumanPlayer(final Model model) {
        return model.getPlayers().stream().filter(p -> p.isHuman()).findAny()
                .orElseThrow(() -> new MicroColException("There is no human player."));
    }

    protected boolean playerHaveMoreOrEqualColonies(final Model model, final int numberOfColonies) {
        return model.getColonies(getHumanPlayer(model)).size() >= numberOfColonies;
    }

    protected int getNumberOfMilitaryUnits(final Model model) {
        return getNumberOfMilitaryUnitsForPlayer(getHumanPlayer(model));
    }

    // TODO this info should be provided by player object itself.
    protected int getNumberOfMilitaryUnitsForPlayer(final Player player) {
        return (int) player.getAllUnits().stream().filter(unit -> unit.getType().canAttack())
                .count();
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
        return getContext().saveToMap();
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
