package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Object holds game over result description.
 */
public class GameOverResult {

	private final Player winner;

	private final String gameOverReason;

	GameOverResult(final Player winner, final String gameOverReason) {
		// winner could be null.
		this.winner = winner;
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

}
