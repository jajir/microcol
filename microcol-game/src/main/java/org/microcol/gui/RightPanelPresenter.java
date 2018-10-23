package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.gamepanel.TileWasSelectedController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class RightPanelPresenter {

    private final Logger logger = LoggerFactory.getLogger(RightPanelPresenter.class);

    private final GameModelController gameModelController;

    private final Text text;

    private final RightPanelView display;

    private TileWasSelectedEvent lastFocusedTileEvent;

    @Inject
    RightPanelPresenter(final RightPanelView display, final GameModelController gameModelController,
            final TileWasSelectedController tileWasSelectedController, final Text text,
            final StatusBarMessageController statusBarMessageController,
            final SelectedUnitWasChangedController selectedUnitWasChangedController) {
        this.display = Preconditions.checkNotNull(display);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        display.setNextTurnButtonLabel(text.get("nextTurnButton"));
        display.setNextTurnButtonDisable(true);

        display.getNextTurnButton().setOnAction(e -> {
            logger.debug("Next turn button was pressed");
            display.setNextTurnButtonDisable(true);
            gameModelController.nextTurn();
        });

        display.getNextTurnButton().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("nextTurnButton.desctiption")));
        });

        display.getContent().setOnMouseEntered(e -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("rightPanel.description")));
        });

        tileWasSelectedController.addRunLaterListener(this::onFocusedTile);
        selectedUnitWasChangedController.addRunLaterListener(this::onSelectedUnitWasChanged);
    }

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
        display.setNextTurnButtonLabel(text.get("nextTurnButton"));
        if (gameModelController.isModelReady()) {
            display.setOnMovePlayer(gameModelController.getModel().getCurrentPlayer());
        }
        if (lastFocusedTileEvent != null && gameModelController.isModelReady()) {
            display.refreshView(lastFocusedTileEvent);
        }
    }

    private void onFocusedTile(final TileWasSelectedEvent event) {
        lastFocusedTileEvent = event;
        display.refreshView(event);
    }

}
