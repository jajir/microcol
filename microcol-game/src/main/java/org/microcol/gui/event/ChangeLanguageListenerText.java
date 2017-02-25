package org.microcol.gui.event;

import org.microcol.gui.Text;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ChangeLanguageListenerText implements ChangeLanguageListener {

	private final Text text;

	@Inject
	public ChangeLanguageListenerText(final Text text, final ChangeLanguageController languangeController) {
		this.text = Preconditions.checkNotNull(text);
		languangeController.addLanguageListener(this, ChangeLanguageController.Priority.high);
	}

	@Override
	public void onChangeLanguage(final ChangeLanguageEvent event) {
		text.setLanguage(event.getLanguage());
	}

}