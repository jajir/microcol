package org.microcol.model.event;

import org.microcol.model.Game;

import com.google.common.base.Preconditions;

abstract class GameEvent {
	private final Game game;

	public GameEvent(final Game game) {
		this.game = Preconditions.checkNotNull(game);
	}

	public Game getGame() {
		return game;
	}
}
