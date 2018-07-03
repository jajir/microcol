package org.microcol.model.campaign;

import java.util.Map;

import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

public interface Mission<G extends MissionGoals> {

	/**
	 * Get mission goals.
	 *
	 * @return mission goals
	 */
	G getGoals();

	Map<String, String> saveToMap();

	GameOverResult evaluateGameOver(final Model model);

}