package org.microcol.model.campaign;

import java.util.Map;

import com.google.common.base.Preconditions;

public class Default_1_goals extends MissionGoals {

    void initialize(final Map<String, String> data) {
        Preconditions.checkNotNull(data);
        getGoals().add(new GoalProvider.GoalFindNewWorld(data));
        getGoals().add(new GoalProvider.GoalFoundColony(data));
        getGoals().add(new GoalProvider.GoalSellCigars(data));
    }

    GoalProvider.GoalFindNewWorld getGoalFindNewWorld() {
        return getByClass(GoalProvider.GoalFindNewWorld.class);
    }

    GoalProvider.GoalFoundColony getGoalFoundColony() {
        return getByClass(GoalProvider.GoalFoundColony.class);
    }

    GoalProvider.GoalSellCigars getGoalSellCigars() {
        return getByClass(GoalProvider.GoalSellCigars.class);
    }

}
