package org.microcol.gui.event;

import org.microcol.gui.Text;
import org.microcol.gui.model.World;

public class ChangeLanguageEvent {

	private final Text.Language language;
	
	private final World world;

	public ChangeLanguageEvent(final Text.Language language, final World world) {
		this.language = language;
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public Text.Language getLanguage() {
		return language;
	}

}
