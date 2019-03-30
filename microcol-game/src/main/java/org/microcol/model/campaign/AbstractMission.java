package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.gui.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

abstract class AbstractMission<G extends MissionGoals> {

    private final Model model;

    private final G goals;

    private final GameOverEvaluator gameOverEvaluator;

    private final EventBus eventBus;

    AbstractMission(final MissionCreationContext context, final G goals) {
        this.eventBus = Preconditions.checkNotNull(context.getEventBus());
        this.model = Preconditions.checkNotNull(context.getModel());
        this.goals = Preconditions.checkNotNull(goals);
        gameOverEvaluator = new GameOverEvaluator(eventBus, this::prepareProcessors,
                context.getCampaignMission(), context.getCampaignManager());
    }

    protected void fireEvent(final Object event) {
        getEventBus().post(event);
    }

    private EventBus getEventBus() {
        return eventBus;
    }

    public void onGameFinished(final GameFinishedEvent event) {
        gameOverEvaluator.onGameFinished(event);
    }

    /**
     * list of methods that react on game over event.
     *
     * @return list of functions
     */
    protected abstract List<Function<GameOverProcessingContext, String>> prepareProcessors();

    /**
     * @return the model
     */
    protected Model getModel() {
        return model;
    }

    /**
     * @return the goals
     */
    public G getGoals() {
        return goals;
    }

    public void save(final Map<String, String> out) {
        goals.save(out);
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

    protected int getNumberOfMilitaryUnitsForPlayer(final Player player) {
        return player.getNumberOfMilitaryUnits();
    }

}
