package org.microcol.gui;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameFinishedController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.event.GameFinishedEvent;
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

    private final ViewUtil viewUtil;

    private final Text text;

    private final MainFramePresenter mainFramePresenter;

    private final GameController gameController;

    private final MusicController musicController;

    private final GamePreferences gamePreferences;

    @Inject
    public ApplicationController(final MainFramePresenter mainFramePresenter,
            final GameController gameController,
            final GameFinishedController gameFinishedController,
            final MusicController musicController, final GamePreferences gamePreferences,
            final ViewUtil viewUtil, final Text text) {
        this.mainFramePresenter = Preconditions.checkNotNull(mainFramePresenter);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.musicController = Preconditions.checkNotNull(musicController);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.text = Preconditions.checkNotNull(text);
        gameFinishedController.addListener(event -> gameFinished(event));
    }

    /**
     * It's called only once per application life.
     */
    public void startApplication() {
        logger.debug("Application started.");
        musicController.start(gamePreferences.getVolume());
        mainFramePresenter.showPanel(MainFramePresenter.START_PANEL);
    }

    /**
     * It's called only once per application life.
     */
    public void startNewDefaultGame() {
        logger.debug("Start new default game.");
        gameController.startNewDefaultGame();
        mainFramePresenter.showPanel(MainFramePresenter.MAIN_GAME_PANEL);
    }

    /**
     * It's called when game finished and start navigation should be shown.
     */
    private void gameFinished(final GameFinishedEvent event) {
        logger.debug("Game finished.");
        new DialogGameOver(viewUtil, text, event);
        mainFramePresenter.showPanel(MainFramePresenter.START_PANEL);
    }

}
