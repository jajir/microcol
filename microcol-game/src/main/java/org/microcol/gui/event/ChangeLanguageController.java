package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.gui.Text;
import org.microcol.gui.model.GameController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Allows to trigger change language event and manage event listeners.
 */
public class ChangeLanguageController {

	private final Logger logger = Logger.getLogger(ChangeLanguageController.class);

	private final List<ChangeLanguageListener> listeners = new ArrayList<ChangeLanguageListener>();

	private final GameController gameController;

	/**
	 * Priority allows to call high priority events as first.
	 */
	public static enum Priority {
		high, low
	}

	@Inject
	public ChangeLanguageController(final GameController gameController) {
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	public void addLanguageListener(final ChangeLanguageListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void addLanguageListener(final ChangeLanguageListener listener, final Priority priority) {
		Preconditions.checkNotNull(listener);
		Preconditions.checkNotNull(priority);
		if (priority.equals(Priority.high)) {
			listeners.add(0, listener);
		} else {
			listeners.add(listener);
		}
	}

	public void fireLanguageWasChangedEvent(final Text.Language language) {
		Preconditions.checkNotNull(language);
		ChangeLanguageEvent event = new ChangeLanguageEvent(language, gameController.getGame());
		logger.debug("firing event language was changed: " + language);
		listeners.forEach(listener -> {
			listener.onChangeLanguage(event);
		});
	}
}
