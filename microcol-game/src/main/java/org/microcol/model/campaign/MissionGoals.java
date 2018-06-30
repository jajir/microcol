package org.microcol.model.campaign;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds mission goals.
 */
public abstract class MissionGoals {

    private final List<MissionGoal> goals = new ArrayList<>();

    /**
     * @return the goals
     */
    public List<MissionGoal> getGoals() {
        return goals;
    }

    @SuppressWarnings("unchecked")
    public <G extends MissionGoal> G getByClass(final Class<?> clazz) {
        Optional<MissionGoal> oGoal = goals.stream().filter(goal -> goal.getClass().equals(clazz))
                .findFirst();
        if (oGoal.isPresent()) {
            return (G) oGoal.get();
        } else {
            throw new InvalidParameterException(
                    String.format("There is goal defined by class '%s'", clazz.getName()));
        }
    }

    void save(final Map<String, String> data) {
        goals.forEach(goal -> goal.save(data));
    }

    boolean isAllGoalsDone() {
        return !goals.stream().filter(goal -> !goal.isFinished()).findAny().isPresent();
    }

}
