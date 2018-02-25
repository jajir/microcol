package org.microcol.model;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Evaluate game over conditions.
 */
public class GameOverEvaluator {

	private final ChainOfCommandOptionalStrategy<Model, GameOverResult> conditions = new ChainOfCommandOptionalStrategy<>(
			Lists.newArrayList(this::verifyCalendar, this::verifyHumanPlayerLostAllColonies));

	public Optional<GameOverResult> evaluate(final Model model) {
		return conditions.apply(model);
	}

	private GameOverResult verifyCalendar(final Model model) {
		if (model.getCalendar().isFinished()) {
			return new GameOverResult(null, null, GameOverResult.REASON_TIME_IS_UP);
		} else {
			return null;
		}
	}

	private GameOverResult verifyHumanPlayerLostAllColonies(final Model model) {
		if (model.getCalendar().getNumberOfPlayedTurns() > 15) {
			for (final Player player : model.getPlayerStore().getPlayers()) {
				if (player.isHuman() && model.getColonies(player).isEmpty()) {
					return new GameOverResult(null, player, GameOverResult.REASON_NO_COLONIES);
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("hashcode", hashCode())
				.toString();
	}	

}
