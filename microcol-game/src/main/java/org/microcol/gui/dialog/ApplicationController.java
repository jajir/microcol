package org.microcol.gui.dialog;

import org.microcol.gui.MusicController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.FreePlay_missionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Control application state. It start or load new game. It control content of
 * main application screen.
 */
public final class ApplicationController {

    private final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final GameController gameController;

    private final MusicController musicController;

    private final GamePreferences gamePreferences;

    private final EventBus eventBus;

    @Inject
    public ApplicationController(final GameController gameController,
            final MusicController musicController, final GamePreferences gamePreferences, final EventBus eventBus) {
        this.gameController = Preconditions.checkNotNull(gameController);
        this.musicController = Preconditions.checkNotNull(musicController);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    /**
     * It's called only once per application life.
     */
    public void startMusic() {
        logger.debug("Application started.");
        musicController.start(gamePreferences.getVolume());
    }

    /**
     * It's called only once per application life.
     */
    public void startNewFreeGame() {
        logger.debug("Start new default game.");
        gameController.startCampaignMission(CampaignNames.freePlay, FreePlay_missionNames.freePlay);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }
    
    

}
