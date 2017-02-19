package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.World;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class LanguangeController {

	private final Logger logger = Logger.getLogger(LanguangeController.class);

	private final List<LanguageListener> listeners = new ArrayList<LanguageListener>();

	private final Text text;

	private final GamePreferences gamePreferences;

	private final World world;

	@Inject
	public LanguangeController(final Text text, final GamePreferences gamePreferences, final World world) {
		this.text = Preconditions.checkNotNull(text);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.world = Preconditions.checkNotNull(world);
	}

	public void addLanguageListener(final LanguageListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireLanguageWasChangedEvent(final Text.Language language) {
		Preconditions.checkNotNull(language);
		text.setLanguage(language);
		gamePreferences.setLanguage(language);
		ChangeLanguageEvent event = new ChangeLanguageEvent(world);
		logger.debug("firing event language was changed: " + language);
		listeners.forEach(listener -> {
			listener.onChangeLanguage(event);
		});
	}
}
