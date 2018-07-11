package org.microcol.model.campaign;

import java.util.Map;

/**
 * Contains mission goals instances.
 */
public final class GoalProvider {

    public static class GoalFindNewWorld extends MissionGoal {

	GoalFindNewWorld(final Map<String, String> data) {
	    super("findNewWorld", "campaign.default.m0.g1", data);
	}

    }

    public static class GoalFoundColony extends MissionGoal {

	GoalFoundColony(final Map<String, String> data) {
	    super("foundColony", "campaign.default.m0.g2", data);
	}

    }

    public static class GoalProduceCigars extends MissionGoal {

	GoalProduceCigars(final Map<String, String> data) {
	    super("produceCigars", "campaign.default.m0.g3", data);
	}

    }

    public static class GoalSellCigars extends MissionGoal {

	private final static String MAP_KEY_SOLD_CIGARS = "cigarsWasSold";

	private int alreadyWasSold;

	GoalSellCigars(final Map<String, String> data) {
	    super("sellCigars", "campaign.default.m0.g4", data);
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
	    super("numberOfColonies", "campaign.default.m1.g1", data);
	}

    }

    public static class GoalAmountOfGold extends MissionGoal {

	GoalAmountOfGold(final Map<String, String> data) {
	    super("amountOfGold", "campaign.default.m1.g2", data);
	}

    }

    public static class GoalMilitaryPower extends MissionGoal {

	GoalMilitaryPower(final Map<String, String> data) {
	    super("militaryPower", "campaign.default.m1.g3", data);
	}

    }

    public static class GoalDeclareIndependence extends MissionGoal {

	GoalDeclareIndependence(final Map<String, String> data) {
	    super("militaryPower", "campaign.default.m2.g1", data);
	}

    }

    public static class GoalConquerRaf extends MissionGoal {

	GoalConquerRaf(final Map<String, String> data) {
	    super("militaryPower", "campaign.default.m2.g2", data);
	}

    }

}
