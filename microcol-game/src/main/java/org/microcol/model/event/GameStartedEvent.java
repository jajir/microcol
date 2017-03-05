package org.microcol.model.event;

import org.microcol.model.Game;

import com.google.common.base.MoreObjects;

// TODO JKA Documentation
public class GameStartedEvent extends ModelEvent {
	public GameStartedEvent(final Game game) {
		super(game);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.toString();
	}
}
