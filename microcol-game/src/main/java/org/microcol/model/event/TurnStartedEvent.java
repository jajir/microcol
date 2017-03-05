package org.microcol.model.event;

import org.microcol.model.Game;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

// TODO JKA Documentation
public class TurnStartedEvent extends ModelEvent {
	private final Player player;

	public TurnStartedEvent(final Game game, final Player player) {
		super(game);

		this.player = Preconditions.checkNotNull(player);
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("player", player)
			.toString();
	}
}
