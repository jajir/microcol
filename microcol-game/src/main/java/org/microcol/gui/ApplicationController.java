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

	@Inject
	public ApplicationController(final MainFramePresenter mainFramePresenter, final GameController gameController,
			final GameFinishedController gameFinishedController) {
		this.mainFramePresenter = Preconditions.checkNotNull(mainFramePresenter);
		this.gameController = Preconditions.checkNotNull(gameController);
		gameFinishedController.addListener(event -> gameFinished());
	}

	public void startNewGame() {
		logger.debug("Game started.");
		gameController.newGame();
		mainFramePresenter.showPanel(MainFramePresenter.MAIN_GAME_PANEL);
	}

	private void gameFinished() {
		logger.debug("Game finished.");
		mainFramePresenter.showPanel(MainFramePresenter.START_PANEL);
	}

}
