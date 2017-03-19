package org.microcol.gui.event;

import org.microcol.gui.Text;
import org.microcol.model.Model;

public class ChangeLanguageEvent {

	private final Text.Language language;

	private final Model game;

	public ChangeLanguageEvent(final Text.Language language, final Model game) {
		this.language = language;
		this.game = game;
	}

	public Model getGame() {
		return game;
	}

	public Text.Language getLanguage() {
		return language;
	}

}
