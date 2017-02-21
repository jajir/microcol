package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.gui.model.GameController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class LanguangeController {

	private final Logger logger = Logger.getLogger(LanguangeController.class);

	private final List<LanguageListener> listeners = new ArrayList<LanguageListener>();

	private final Text text;

	private final GamePreferences gamePreferences;

	private final GameController gameController;

	@Inject
	public LanguangeController(final Text text, final GamePreferences gamePreferences,
			final GameController gameController) {
		this.text = Preconditions.checkNotNull(text);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	public void addLanguageListener(final LanguageListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireLanguageWasChangedEvent(final Text.Language language) {
		Preconditions.checkNotNull(language);
		text.setLanguage(language);
		gamePreferences.setLanguage(language);
		ChangeLanguageEvent event = new ChangeLanguageEvent(gameController.getWorld());
		logger.debug("firing event language was changed: " + language);
		listeners.forEach(listener -> {
			listener.onChangeLanguage(event);
		});
	}
}
