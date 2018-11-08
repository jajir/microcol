package org.microcol.gui.buttonpanel;

import org.microcol.gui.util.Listener;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Listener
public class ButtonPanelController {
    
    private final Logger LOGGER = LoggerFactory.getLogger(ButtonPanelController.class);

    private final ButtonPanel buttonPanel;

    @Inject
    ButtonPanelController(final ButtonPanel buttonPanel) {
        this.buttonPanel = Preconditions.checkNotNull(buttonPanel);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        LOGGER.debug("Turn started for player {}", event.getPlayer());
        if (event.getPlayer().isHuman()) {
            buttonPanel.getButtonNextTurn().setDisable(false);
        }
    }
    
}
