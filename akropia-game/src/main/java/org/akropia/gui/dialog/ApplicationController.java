package org.akropia.gui.dialog;

import org.akropia.gui.MusicController;
import org.akropia.gui.event.model.GameController;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.campaign.FreePlayMap;
import org.akropia.model.generation.GeneratedWorldSettings;
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

    private final Logger logger = LoggerFactory
            .getLogger(ApplicationController.class);

    private final GameController gameController;

    private final MusicController musicController;

    private final GamePreferences gamePreferences;

    private final EventBus eventBus;

    private final PersistingService persistingService;

    @Inject
    public ApplicationController(final GameController gameController,
            final MusicController musicController,
            final GamePreferences gamePreferences, final EventBus eventBus,
            final PersistingService persistingService) {
        this.gameController = Preconditions.checkNotNull(gameController);
        this.musicController = Preconditions.checkNotNull(musicController);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.persistingService = Preconditions.checkNotNull(persistingService);
    }

    /**
     * It's called only once per application life.
     */
    public void startMusic() {
        logger.debug("Application started.");
        musicController.start(gamePreferences.getMusicVolume());
    }

    public void startNewFreeGame(final FreePlayMap map, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation) {
        gameController.startFreePlay(map, playerName, difficulty, nation);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
        persistingService.checkpointNewGame();
    }

    /**
     * @return {@code true} when the generated world started; otherwise the
     *         active game is unchanged and the caller shows the failure
     */
    public boolean startNewGeneratedGame(final GeneratedWorldSettings settings,
            final String playerName, final Difficulty difficulty,
            final EuropeanNation nation) {
        if (!gameController.startGeneratedFreePlay(settings, playerName,
                difficulty, nation)) {
            return false;
        }
        eventBus.post(new ShowScreenEvent(Screen.GAME));
        persistingService.checkpointNewGame();
        return true;
    }

}
