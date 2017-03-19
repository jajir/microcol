package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.Preconditions;

abstract class ModelEvent {
	private final Model model;

	ModelEvent(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	public Model getModel() {
		return model;
	}
}
