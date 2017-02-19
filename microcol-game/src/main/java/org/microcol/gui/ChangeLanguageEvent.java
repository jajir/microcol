package org.microcol.gui;

import org.microcol.model.World;

public class ChangeLanguageEvent {

	private final World world;

	public ChangeLanguageEvent(final World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

}
