package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Abstract mission. Contain some basic functionality.
 */
public class MissionImpl<G extends MissionGoals> implements Mission<G> {

	public final static String GAME_OVER_REASON_ALL_GOALS_ARE_DONE = "gameOverReasonAllGoalsAreDone";

	private final MissionName missionName;

	private final Integer orderNo;

	private final MissionDefinition<G> missionDefinition;

	private final CampaignManager campaignManager;

	MissionImpl(final MissionName missionName, final Integer orderNo, MissionDefinition<G> missionDefinition,
			final ModelPo modelPo, final CampaignManager campaignManager) {
		this.missionName = Preconditions.checkNotNull(missionName);
		this.orderNo = Preconditions.checkNotNull(orderNo);
		this.missionDefinition = Preconditions.checkNotNull(missionDefinition);
		this.campaignManager = Preconditions.checkNotNull(campaignManager);
		missionDefinition.getModel().addListener(getMissionDefinition());
		initialize(modelPo);
	}

	private void initialize(final ModelPo modelPo) {
		Preconditions.checkNotNull(modelPo, "Model persistent object is null");
		if (modelPo.getCampaign().getData() == null) {
			getGoals().initialize(new HashMap<>());
		} else {
			getGoals().initialize(modelPo.getCampaign().getData());
		}
	}

	// TODO there is lot of dependencies to save mission result
	protected void setFinished(final boolean finished) {
		Preconditions.checkNotNull(campaignManager, "campaignManager is null");
		final Campaign campaign = campaignManager.getCampaignByName(missionName.getCampaignName());
		final CampaignMission campaignMission = campaign.getMisssionByName(missionName.getName());
		campaignMission.setFinished(finished);
		campaignManager.saveMissionState();
	}

	@Override
	public G getGoals() {
		return missionDefinition.goals;
	}

	protected Integer get(final Map<String, String> map, final String key) {
		String val = map.get(key);
		if (val == null) {
			return 0;
		} else {
			return Integer.parseInt(val);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass()).add("orderNo", orderNo).add("missionName", missionName)
				.toString();
	}

	/**
	 * Get list of functions that evaluate game over conditions. When one function
	 * return nun-null return than game is over and returned object is passed in
	 * game over event to frontend.
	 *
	 * @return list of game over evaluators
	 */
	public static List<Function<Model, GameOverResult>> getPredefinedGameOverEvaluators() {
		return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
				GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
	}

	@Override
	public GameOverResult evaluateGameOver(final Model model) {
		if (getGoals().isAllGoalsDone()) {
			setFinished(true);
			return new GameOverResult(GAME_OVER_REASON_ALL_GOALS_ARE_DONE);
		}
		return null;
	}

	@Override
	public Map<String, String> saveToMap() {
		final Map<String, String> out = new HashMap<String, String>();
		// TODO call it directly
		missionDefinition.goals.save(out);
		return out;
	}

	/**
	 * @return the missionDefinition
	 */
	public MissionDefinition<G> getMissionDefinition() {
		return missionDefinition;
	}

}
