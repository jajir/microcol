package org.microcol.gui.event;

import org.microcol.gui.AboutDialog;
import org.microcol.gui.Text;
import org.microcol.gui.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Provide about game event listener. Listener open about game dialog.
 */
public class AboutGameListenerImpl implements Listener<AboutGameEvent> {

	private final ViewUtil viewUtil;

	private final Text text;

	@Inject
	public AboutGameListenerImpl(final AboutGameEventController gameEventController, final ViewUtil viewUtil,
			final Text text) {
		gameEventController.addListener(this);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
	}

	@Override
	public void onEvent(final AboutGameEvent event) {
		new AboutDialog(viewUtil, text).setVisible(true);
	}

}
