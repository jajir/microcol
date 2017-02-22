package org.microcol.model.event;

import org.microcol.model.Game;
import org.microcol.model.Player;

public class TurnStartedEvent extends GameEvent {
	private final Player player;

	public TurnStartedEvent(final Game game, final Player player) {
		super(game);

		// TODO JKA Add not null test.
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("TurnStartedEvent [player = ");
		builder.append(player);
		builder.append("]");

		return builder.toString();
	}
}
