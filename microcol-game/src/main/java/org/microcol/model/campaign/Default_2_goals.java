package org.microcol.model.campaign;

import java.util.Map;

import com.google.common.base.Preconditions;

public class Default_2_goals extends MissionGoals {

    @Override
    void initialize(final Map<String, String> data) {
	Preconditions.checkNotNull(data);
	getGoals().add(new GoalProvider.GoalDeclareIndependence(data));
	getGoals().add(new GoalProvider.GoalConquerRaf(data));
    }

    GoalProvider.GoalDeclareIndependence getGoalDeclareIndependence() {
	return getByClass(GoalProvider.GoalDeclareIndependence.class);
    }

    GoalProvider.GoalConquerRaf getGoalConquerRaf() {
	return getByClass(GoalProvider.GoalConquerRaf.class);
    }

}
