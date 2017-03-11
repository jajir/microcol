package org.microcol.gui.event;

import org.apache.log4j.Logger;
import org.microcol.gui.AboutDialog;
import org.microcol.gui.Text;
import org.microcol.gui.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameEventListenerImpl implements GameEventListener {

	private final Logger logger = Logger.getLogger(GameEventListenerImpl.class);

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
	public void onGameExit() {
		logger.debug("onGameExit was triggered");
	}

	@Override
	public void onAboutGame() {
		new AboutDialog(viewUtil, text).setVisible(true);
	}

}
