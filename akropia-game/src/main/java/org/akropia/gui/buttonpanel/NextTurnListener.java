package org.akropia.gui.buttonpanel;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.dialog.EndTurnWarningsDialog;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provider;
import org.akropia.gui.util.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public class NextTurnListener {

    private final Logger LOGGER = LoggerFactory
            .getLogger(NextTurnListener.class);

    private final GameModelController gameModelController;

    private final Provider<EndTurnWarningsDialog> warningsDialog;
    private final EventBus eventBus;
    private boolean handlingRequest;

    @Inject
    NextTurnListener(final GameModelController gameModelController,
            final Provider<EndTurnWarningsDialog> warningsDialog,
            final EventBus eventBus) {
        this.warningsDialog = warningsDialog;
        this.eventBus = eventBus;
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
    }

    @Subscribe
    private void onRequestNextTurn(final RequestNextTurnEvent event) {
        if (handlingRequest || !gameModelController.isHumanTurnReady()) {
            return;
        }
        handlingRequest = true;
        try {
            final var warnings = gameModelController.getModel()
                    .getIdleColonistsBeforeEndTurn();
            if (warnings.isEmpty() || warningsDialog.get().confirm(warnings,
                    colony -> eventBus.post(
                            new ShowScreenEvent(Screen.COLONY, colony)))) {
                eventBus.post(new NextTurnEvent());
            }
        } finally {
            handlingRequest = false;
        }
    }

    @Subscribe
    private void onNextTurn(
            @SuppressWarnings("unused") final NextTurnEvent event) {
        LOGGER.debug("Next turn button was pressed");
        gameModelController.nextTurn();
    }

}
