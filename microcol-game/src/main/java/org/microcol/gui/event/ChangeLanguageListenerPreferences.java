package org.microcol.gui.event;

import org.microcol.gui.GamePreferences;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ChangeLanguageListenerPreferences implements Listener<ChangeLanguageEvent> {

	private final GamePreferences gamePreferences;

	@Inject
	public ChangeLanguageListenerPreferences(final GamePreferences gamePreferences,
			final ChangeLanguageController languangeController) {
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		languangeController.addListener(this);
	}

	@Override
	public void onEvent(final ChangeLanguageEvent event) {
		gamePreferences.setLanguage(event.getLanguage().getLocale());
	}

}
