package org.microcol.gui;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ChangeLanguageListenerText implements ChangeLanguageListener {

	private final Text text;

	@Inject
	public ChangeLanguageListenerText(final Text text, final LanguangeController languangeController) {
		this.text = Preconditions.checkNotNull(text);
		languangeController.addLanguageListener(this);
	}

	@Override
	public void onChangeLanguage(final ChangeLanguageEvent event) {
		text.setLanguage(event.getLanguage());
	}

}
