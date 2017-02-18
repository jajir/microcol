package org.microcol.gui;

import org.apache.log4j.Logger;

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
		System.exit(0);
	}

	@Override
	public void onAboutGame() {
		new AboutDialog(viewUtil, text).setVisible(true);
	}

}
