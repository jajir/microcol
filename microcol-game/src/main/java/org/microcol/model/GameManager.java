package org.microcol.model;

import com.google.common.base.Preconditions;

class GameManager {
	private final Model model;

	private boolean started;
	private Player currentPlayer;

	GameManager(final Model model) {
		this.model = model;
	}

	boolean isStarted() {
		return started;
	}

	boolean isFinished() {
		return model.getCalendar().isFinished();
	}

	boolean isActive() {
		return started && !isFinished();
	}

	void checkGameActive() {
		if (!isActive()) {
			throw new IllegalStateException("Game must be active.");
		}
	}

	void checkCurrentPlayer(final Player player) {
		Preconditions.checkNotNull(player);

		if (!player.equals(currentPlayer)) {
			throw new IllegalStateException(String.format("This player (%s) is not current player (%s).", player, currentPlayer));
		}
	}

	Player getCurrentPlayer() {
		Preconditions.checkState(started, "Game must be started.");

		return currentPlayer;
	}

	void startGame() {
		Preconditions.checkState(!started, "Game was already started.");

		started = true;
		currentPlayer = model.getPlayers().get(0);
		model.fireGameStarted();
		model.fireRoundStarted();
		currentPlayer.startTurn();
		model.fireTurnStarted(currentPlayer);
	}

	void endTurn() {
		checkGameActive();

		final int index = model.getPlayers().indexOf(currentPlayer);
		if (index < model.getPlayers().size() - 1) {
			currentPlayer = model.getPlayers().get(index + 1);
			currentPlayer.startTurn();
			model.fireTurnStarted(currentPlayer);
		} else {
			model.getCalendar().endRound();
			if (!model.getCalendar().isFinished()) {
				currentPlayer = model.getPlayers().get(0);
				model.fireRoundStarted();
				currentPlayer.startTurn();
				model.fireTurnStarted(currentPlayer);
			} else {
				model.fireGameFinished();
			}
		}
	}
}
