package org.microcol.model.campaign;

import java.util.Map;

/**
 * Contains mission goals instances.
 */
public class GoalProvider {

    public static class GoalFindNewWorld extends MissionGoal {

        GoalFindNewWorld(final Map<String, String> data) {
            super("findNewWorld", "campaign.default.start", data);
        }

    }

    public static class GoalFoundColony extends MissionGoal {

        GoalFoundColony(final Map<String, String> data) {
            super("foundColony", "campaign.default.continentInSight", data);
        }

    }

    public static class GoalSellCigars extends MissionGoal {

        GoalSellCigars(final Map<String, String> data) {
            super("sellCigars", "campaign.default.produce100cigars", data);
        }

    }

}
