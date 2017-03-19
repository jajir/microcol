package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

public class GameStartedEvent extends ModelEvent {
	public GameStartedEvent(final Model model) {
		super(model);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.toString();
	}
}
