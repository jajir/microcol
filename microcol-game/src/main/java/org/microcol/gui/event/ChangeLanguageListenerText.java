package org.microcol.gui.event;

import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class connect changing of language to resource bundle. When language is
 * changed than class force {@link Text} to re-load resource bundle.
 */
public class ChangeLanguageListenerText implements Listener<ChangeLanguageEvent> {

	private final Text text;

	@Inject
	public ChangeLanguageListenerText(final Text text, final ChangeLanguageController languangeController) {
		this.text = Preconditions.checkNotNull(text);
		languangeController.addListener(this, 1);
	}

	@Override
	public void onEvent(final ChangeLanguageEvent event) {
		text.setLanguage(event.getLanguage());
	}

}
