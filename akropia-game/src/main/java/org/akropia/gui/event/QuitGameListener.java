package org.akropia.gui.event;

import org.akropia.gui.MusicController;
import org.akropia.gui.SuccessCuePlayer;
import org.akropia.gui.util.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Class wire up events. Class should not be used. Only after instantiated after
 * application startup.
 */
@Listener
public final class QuitGameListener {

    private final Logger logger = LoggerFactory
            .getLogger(QuitGameListener.class);

    private final MusicController musicController;
    private final org.akropia.gui.dialog.PersistingService saves;

    private final SuccessCuePlayer successCuePlayer;

    @Inject
    public QuitGameListener(final MusicController musicController,
            final org.akropia.gui.dialog.PersistingService saves,
            final SuccessCuePlayer successCuePlayer) {
        this.saves = saves;
        this.musicController = Preconditions.checkNotNull(musicController);
        this.successCuePlayer = Preconditions.checkNotNull(successCuePlayer);
    }

    @Subscribe
    private void onQuitGame(
            @SuppressWarnings("unused") final QuitGameEvent event) {
        if (!saves.confirmLeave()) {
            return;
        }
        logger.info("Shutting down Akropia");
        musicController.stop();
        successCuePlayer.stop();
        System.exit(0);
    }

}
