package org.microcol.gui;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class GameEventListenerImpl implements GameEventListener {

	private final Logger logger = Logger.getLogger(GameEventListenerImpl.class);

	@Inject
	public GameEventListenerImpl(final GameEventController gameEventController) {
		gameEventController.addGameEventListener(this);
	}

	@Override
	public void onGameExit() {
		logger.debug("onGameExit was triggered");
		System.exit(0);
	}

	@Override
	public void onAboutGame() {
		//TODO add some about game dialog
	}

}
