package org.microcol.gui;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.GamePreferences;
import org.microcol.model.campaign.FreePlay_campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control application state. It start or load new game. It control content of
 * main application screen.
 * 
 * 
 * 
 * TODO refactor it, it start's game after application start up.
 */
public class ApplicationController {

    private final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final GameController gameController;

    private final MusicController musicController;

    private final GamePreferences gamePreferences;

    @Inject
    public ApplicationController(final GameController gameController,
            final MusicController musicController, final GamePreferences gamePreferences) {
        this.gameController = Preconditions.checkNotNull(gameController);
        this.musicController = Preconditions.checkNotNull(musicController);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    /**
     * It's called only once per application life.
     */
    public void startApplication() {
        logger.debug("Application started.");
        musicController.start(gamePreferences.getVolume());
    }

    /**
     * It's called only once per application life.
     */
    public void startNewFreeGame() {
        logger.debug("Start new default game.");
        gameController.startCampaignMission(FreePlay_campaign.FREE_PLAY,
                FreePlay_campaign.FREE_PLAY);
    }

}
