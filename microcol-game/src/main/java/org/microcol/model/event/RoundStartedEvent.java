package org.microcol.model.event;

import org.microcol.model.Game;

public class RoundStartedEvent extends GameEvent {
	public RoundStartedEvent(final Game game) {
		super(game);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("RoundStartedEvent []");

		return builder.toString();
	}
}
