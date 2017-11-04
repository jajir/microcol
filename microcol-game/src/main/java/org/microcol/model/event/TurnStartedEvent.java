package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class TurnStartedEvent extends ModelEvent {
	
	private final Player player;

	public TurnStartedEvent(final Model model, final Player player) {
		super(model);

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
