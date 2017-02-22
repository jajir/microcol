package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.gui.model.GameController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class LanguangeController {

	private final Logger logger = Logger.getLogger(LanguangeController.class);

	private final List<ChangeLanguageListener> listeners = new ArrayList<ChangeLanguageListener>();

	private final GameController gameController;

	@Inject
	public LanguangeController(final GameController gameController) {
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	public void addLanguageListener(final ChangeLanguageListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireLanguageWasChangedEvent(final Text.Language language) {
		Preconditions.checkNotNull(language);
		ChangeLanguageEvent event = new ChangeLanguageEvent(language, gameController.getWorld());
		logger.debug("firing event language was changed: " + language);
		listeners.forEach(listener -> {
			listener.onChangeLanguage(event);
		});
	}
}
