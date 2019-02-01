package org.microcol.model.campaign;

import java.util.Map;

/**
 * Contains mission goals instances.
 */
public final class GoalProvider {

    public static class GoalFindNewWorld extends MissionGoal {

        GoalFindNewWorld(final Map<String, String> data) {
            super("findNewWorld", Missions.default_m0_g1, data);
        }

    }

    public static class GoalFoundColony extends MissionGoal {

        GoalFoundColony(final Map<String, String> data) {
            super("foundColony", Missions.default_m0_g2, data);
        }

    }

    public static class GoalProduceCigars extends MissionGoal {

        GoalProduceCigars(final Map<String, String> data) {
            super("produceCigars", Missions.default_m0_g3, data);
        }

    }

    public static class GoalSellCigars extends MissionGoal {

        private final static String MAP_KEY_SOLD_CIGARS = "cigarsWasSold";

        private int alreadyWasSold;

        GoalSellCigars(final Map<String, String> data) {
            super("sellCigars", Missions.default_m0_g4, data);
            alreadyWasSold = getInt(data, MAP_KEY_SOLD_CIGARS);
        }

        @Override
        void save(Map<String, String> data) {
            super.save(data);
            data.put(MAP_KEY_SOLD_CIGARS, String.valueOf(alreadyWasSold));
        }

        /**
         * @return the alreadyWasSold
         */
        public int getWasSold() {
            return alreadyWasSold;
        }

        /**
         * @param alreadyWasSold
         *            the alreadyWasSold to set
         */
        public void setWasSold(int alreadyWasSold) {
            this.alreadyWasSold = alreadyWasSold;
        }

    }

    public static class GoalNumberOfColonies extends MissionGoal {

        GoalNumberOfColonies(final Map<String, String> data) {
            super("numberOfColonies", Missions.default_m1_g1, data);
        }

    }

    public static class GoalAmountOfGold extends MissionGoal {

        GoalAmountOfGold(final Map<String, String> data) {
            super("amountOfGold", Missions.default_m1_g2, data);
        }

    }

    public static class GoalMilitaryPower extends MissionGoal {

        GoalMilitaryPower(final Map<String, String> data) {
            super("militaryPower", Missions.default_m1_g3, data);
        }

    }

    public static class GoalDeclareIndependence extends MissionGoal {

        GoalDeclareIndependence(final Map<String, String> data) {
            super("declareIndependence", Missions.default_m2_g1, data);
        }

    }

    public static class GoalConquerRaf extends MissionGoal {

        GoalConquerRaf(final Map<String, String> data) {
            super("conquerRaf", Missions.default_m2_g2, data);
        }

    }

}
