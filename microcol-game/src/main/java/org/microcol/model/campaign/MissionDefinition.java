package org.microcol.model.campaign;

import java.util.Map;

import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;

public abstract class MissionDefinition<G extends MissionGoals>
        extends AbstractModelListenerAdapter {

    private final Model model;

    final protected G goals;

    MissionDefinition(final MissionCallBack missionCallBack, final Model model, final G goals) {
        super(missionCallBack);
        this.model = Preconditions.checkNotNull(model);
        this.goals = Preconditions.checkNotNull(goals);
    }

    /**
     * @return the model
     */
    public Model getModel() {
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
