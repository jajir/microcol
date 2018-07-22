package org.microcol.model.campaign;

import java.util.Map;

import com.google.common.base.Preconditions;

public final class Default_1_goals extends MissionGoals {

    @Override
    void initialize(final Map<String, String> data) {
        Preconditions.checkNotNull(data);
        getGoals().add(new GoalProvider.GoalNumberOfColonies(data));
        getGoals().add(new GoalProvider.GoalAmountOfGold(data));
        getGoals().add(new GoalProvider.GoalMilitaryPower(data));
    }

    GoalProvider.GoalNumberOfColonies getGoalNumberOfColonies() {
        return getByClass(GoalProvider.GoalNumberOfColonies.class);
    }

    GoalProvider.GoalAmountOfGold getGoalAmountOfGold() {
        return getByClass(GoalProvider.GoalAmountOfGold.class);
    }

    GoalProvider.GoalMilitaryPower getGoalMilitaryPower() {
        return getByClass(GoalProvider.GoalMilitaryPower.class);
    }

}
