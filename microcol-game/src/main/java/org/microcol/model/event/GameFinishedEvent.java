package org.microcol.model.event;

import org.microcol.model.Game;

import com.google.common.base.MoreObjects;

public class GameFinishedEvent extends ModelEvent {
	public GameFinishedEvent(final Game game) {
		super(game);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.toString();
	}
}
