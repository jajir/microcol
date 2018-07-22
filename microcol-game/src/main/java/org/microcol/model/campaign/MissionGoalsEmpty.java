package org.microcol.model.campaign;

import java.util.Map;

/**
 * It's empty mission goals implementation.
 */
public final class MissionGoalsEmpty extends MissionGoals {

    @Override
    void initialize(final Map<String, String> data) {
        // just empty implementation.
    }

    @Override
    boolean isAllGoalsDone() {
        /*
         * Return false and goals will be always evaluated as not finished.
         */
        return false;
    }
}
