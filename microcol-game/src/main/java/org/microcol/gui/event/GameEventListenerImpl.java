package org.microcol.gui.event;

import org.microcol.gui.AboutDialog;
import org.microcol.gui.Text;
import org.microcol.gui.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

//TODO JJ add some generic super class for controllers.
//TODO JJ generic controller should support parametrized event method
public class GameEventListenerImpl implements GameEventListener {

	private final ViewUtil viewUtil;

	private final Text text;

	@Inject
	public GameEventListenerImpl(final GameEventController gameEventController, final ViewUtil viewUtil,
			final Text text) {
		gameEventController.addGameEventListener(this);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
	}

	@Override
	public void onAboutGame() {
		new AboutDialog(viewUtil, text).setVisible(true);
	}

}
