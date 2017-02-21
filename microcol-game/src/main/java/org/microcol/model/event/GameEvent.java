package org.microcol.model.event;

import org.microcol.model.Game;

public abstract class GameEvent {
	private final Game game;

	public GameEvent(final Game game) {
		// TODO JKA Add not null test.
		this.game = game;
	}

	public Game getGame() {
		return game;
	}
}
