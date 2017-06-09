package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

public final class GameFinishedEvent extends ModelEvent {
	public GameFinishedEvent(final Model model) {
		super(model);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.toString();
	}
}
