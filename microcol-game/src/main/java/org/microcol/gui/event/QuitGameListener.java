package org.microcol.gui.event;

import org.microcol.gui.MusicController;
import org.microcol.gui.util.Listener;
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
    private final org.microcol.gui.dialog.PersistingService saves;

    @Inject
    public QuitGameListener(final MusicController musicController,
            final org.microcol.gui.dialog.PersistingService saves) {
        this.saves = saves;
        this.musicController = Preconditions.checkNotNull(musicController);
    }

    @Subscribe
    private void onQuitGame(
            @SuppressWarnings("unused") final QuitGameEvent event) {
        if (!saves.confirmLeave()) {
            return;
        }
        logger.info("Shutting down MicroCol");
        musicController.stop();
        System.exit(0);
    }

}
