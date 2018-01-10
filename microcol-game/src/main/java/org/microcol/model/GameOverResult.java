package org.microcol.model;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Object holds game over result description.
 */
public class GameOverResult {

	public final static String REASON_TIME_IS_UP = "TIME_IS_UP";

	public final static String REASON_NO_COLONIES = "NO_COLONIES";

	private final Player winner;

	private final Player looser;

	private final String gameOverReason;

	GameOverResult(final Player winner, final Player looser, final String gameOverReason) {
		// winner could be null.
		this.winner = winner;
		this.looser = looser;
		this.gameOverReason = Preconditions.checkNotNull(gameOverReason);
	}

	/**
	 * @return the winner
	 */
	public Optional<Player> getWinner() {
		return Optional.ofNullable(winner);
	}

	/**
	 * @return the gameOverReason
	 */
	public String getGameOverReason() {
		return gameOverReason;
	}

	/**
	 * @return the looser
	 */
	public Optional<Player> getLooser() {
		return Optional.ofNullable(looser);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("game", gameOverReason)
				.add("winner", winner)
				.add("looser", looser)
				.toString();
	}	

}
