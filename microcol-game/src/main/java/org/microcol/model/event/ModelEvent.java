package org.microcol.model.event;

import org.microcol.model.Game;

import com.google.common.base.Preconditions;

abstract class ModelEvent {
	private final Game game;

	public ModelEvent(final Game game) {
		this.game = Preconditions.checkNotNull(game);
	}

	public Game getGame() {
		return game;
	}
}
