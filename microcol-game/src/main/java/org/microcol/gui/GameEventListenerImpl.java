package org.microcol.gui;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameEventListenerImpl implements GameEventListener {

	private final Logger logger = Logger.getLogger(GameEventListenerImpl.class);

	private final ViewUtil viewUtil;

	@Inject
	public GameEventListenerImpl(final GameEventController gameEventController, final ViewUtil viewUtil) {
		gameEventController.addGameEventListener(this);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
	}

	@Override
	public void onGameExit() {
		logger.debug("onGameExit was triggered");
		System.exit(0);
	}

	@Override
	public void onAboutGame() {
		new AboutDialog(viewUtil).setVisible(true);
	}

}
