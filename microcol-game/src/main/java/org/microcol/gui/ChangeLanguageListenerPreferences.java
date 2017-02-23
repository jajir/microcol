package org.microcol.gui;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ChangeLanguageListenerPreferences implements ChangeLanguageListener {

	private final GamePreferences gamePreferences;

	@Inject
	public ChangeLanguageListenerPreferences(final GamePreferences gamePreferences,
			final ChangeLanguageController languangeController) {
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		languangeController.addLanguageListener(this);
	}

	@Override
	public void onChangeLanguage(final ChangeLanguageEvent event) {
		gamePreferences.setLanguage(event.getLanguage());
	}

}
