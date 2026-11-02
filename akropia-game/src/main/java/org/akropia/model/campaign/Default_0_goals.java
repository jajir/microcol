package org.akropia.model.campaign;

import java.util.Map;

import com.google.common.base.Preconditions;

final class Default_0_goals extends MissionGoals {

    Default_0_goals(final Map<String, String> data) {
        Preconditions.checkNotNull(data);
        getGoals().add(new GoalProvider.GoalFindNewWorld(data));
        getGoals().add(new GoalProvider.GoalFoundColony(data));
        getGoals().add(new GoalProvider.GoalProduceWine(data));
        getGoals().add(new GoalProvider.GoalSellWine(data));
    }

    GoalProvider.GoalFindNewWorld getGoalFindNewWorld() {
        return getByClass(GoalProvider.GoalFindNewWorld.class);
    }

    GoalProvider.GoalFoundColony getGoalFoundColony() {
        return getByClass(GoalProvider.GoalFoundColony.class);
    }

    GoalProvider.GoalProduceWine getGoalProduceWine() {
        return getByClass(GoalProvider.GoalProduceWine.class);
    }

    GoalProvider.GoalSellWine getGoalSellWine() {
        return getByClass(GoalProvider.GoalSellWine.class);
    }

}
