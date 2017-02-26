package org.microcol.gui.event;

import org.microcol.gui.Text;
import org.microcol.model.Game;

public class ChangeLanguageEvent {

	private final Text.Language language;
	
	private final Game game;

	public ChangeLanguageEvent(final Text.Language language, final Game game) {
		this.language = language;
		this.game = game;
	}

	//TODO JJ rename
	public Game getWorld() {
		return game;
	}

	public Text.Language getLanguage() {
		return language;
	}

}
