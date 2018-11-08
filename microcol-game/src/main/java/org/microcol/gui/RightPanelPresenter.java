package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class RightPanelPresenter {

    private final Logger logger = LoggerFactory.getLogger(RightPanelPresenter.class);

    private final GameModelController gameModelController;

    private final RightPanelView display;

    private TileWasSelectedEvent lastFocusedTileEvent;

    @Inject
    RightPanelPresenter(final RightPanelView display, final GameModelController gameModelController,
            final Text text, final EventBus eventBus) {
        this.display = Preconditions.checkNotNull(display);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        display.setNextTurnButtonDisable(true);

        display.getNextTurnButton().setOnAction(e -> {
            logger.debug("Next turn button was pressed");
            display.setNextTurnButtonDisable(true);
            gameModelController.nextTurn();
        });

        display.getNextTurnButton().setOnMouseEntered(event -> {
            eventBus.post(
                    new StatusBarMessageEvent(text.get("nextTurnButton.desctiption"), Source.GAME));
        });

        display.getContent().setOnMouseEntered(e -> {
            eventBus.post(
                    new StatusBarMessageEvent(text.get("rightPanel.description"), Source.GAME));
        });

    }

    @Subscribe
    private void onSelectedUnitWasChanged(final SelectedUnitWasChangedEvent event) {
        if (lastFocusedTileEvent == null) {
            if (event.getSelectedUnit().isPresent()) {
                display.refreshView(event.getSelectedUnit().get().getLocation());
            } else {
                display.cleanView();
            }
        } else {
            display.refreshView(lastFocusedTileEvent.getLocation());
        }
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        logger.debug("Turn started for player {}", event.getPlayer());
        display.setOnMovePlayer(event.getPlayer());
        if (event.getPlayer().isHuman()) {
            display.setNextTurnButtonDisable(false);
            if (lastFocusedTileEvent != null) {
                display.refreshView(lastFocusedTileEvent);
            }
        }
    }

    @SuppressWarnings("unused")
    public void updateLanguage(final I18n i18n) {
        if (gameModelController.isModelReady()) {
            display.setOnMovePlayer(gameModelController.getModel().getCurrentPlayer());
        }
        if (lastFocusedTileEvent != null && gameModelController.isModelReady()) {
            display.refreshView(lastFocusedTileEvent);
        }
    }

    @Subscribe
    private void onFocusedTile(final TileWasSelectedEvent event) {
        lastFocusedTileEvent = event;
        display.refreshView(event);
    }

}
