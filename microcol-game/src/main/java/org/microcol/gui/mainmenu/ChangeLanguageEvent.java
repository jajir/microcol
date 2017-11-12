package org.microcol.gui.mainmenu;

import org.microcol.gui.util.Text;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

public class ChangeLanguageEvent {

	private final Text.Language language;

	private final Model model;

	public ChangeLanguageEvent(final Text.Language language, final Model model) {
		this.language = Preconditions.checkNotNull(language);
		this.model = Preconditions.checkNotNull(model);
	}

	public Model getModel() {
		return model;
	}

	public Text.Language getLanguage() {
		return language;
	}

}
