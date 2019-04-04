package org.microcol.model.campaign;

/**
 * It's empty mission goals implementation.
 */
final class MissionGoalsEmpty extends MissionGoals {

    MissionGoalsEmpty() {
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
