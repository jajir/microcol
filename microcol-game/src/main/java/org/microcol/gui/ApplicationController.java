package org.microcol.gui;

import org.microcol.gui.event.GameController;
import org.microcol.gui.event.GameFinishedController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control application state. It start or load new game. It control content of
 * main application screen.
 */
public class ApplicationController {

	private final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	private final MainFramePresenter mainFramePresenter;

	private final GameController gameController;

	private final MusicController musicController;

	private final GamePreferences gamePreferences;

	@Inject
	public ApplicationController(final MainFramePresenter mainFramePresenter, final GameController gameController,
			final GameFinishedController gameFinishedController, final MusicController musicController,
			final GamePreferences gamePreferences) {
		this.mainFramePresenter = Preconditions.checkNotNull(mainFramePresenter);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.musicController = Preconditions.checkNotNull(musicController);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		gameFinishedController.addListener(event -> gameFinished());
	}

	/**
	 * It's called only once per application life.
	 */
	public void startApplication() {
		logger.debug("Application started.");
		gameController.newGame();
		mainFramePresenter.showPanel(MainFramePresenter.MAIN_GAME_PANEL);
		musicController.start(gamePreferences.getVolume());
	}

	/**
	 * It's called when game finished and start navigation should be shown.
	 */
	private void gameFinished() {
		logger.debug("Game finished.");
		mainFramePresenter.showPanel(MainFramePresenter.START_PANEL);
	}

}
