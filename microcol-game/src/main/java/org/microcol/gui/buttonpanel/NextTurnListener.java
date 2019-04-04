package org.microcol.gui.buttonpanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public class NextTurnListener {

    private final Logger LOGGER = LoggerFactory.getLogger(NextTurnListener.class);

    private final GameModelController gameModelController;

    @Inject
    NextTurnListener(final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onNextTurn(@SuppressWarnings("unused") final NextTurnEvent event) {
        LOGGER.debug("Next turn button was pressed");
        gameModelController.nextTurn();
    }

}
