package org.microcol.gui;

import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.gamepanel.TileWasSelectedController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Text;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class RightPanelPresenter {

    private final Logger logger = LoggerFactory.getLogger(RightPanelPresenter.class);

    private final GameModelController gameModelController;

    private final Text text;

    private final RightPanelView display;

    private TileWasSelectedEvent lastFocusedTileEvent;

    @Inject
    RightPanelPresenter(final RightPanelView display, final GameModelController gameModelController,
            final KeyController keyController,
            final TileWasSelectedController tileWasSelectedController,
            final ChangeLanguageController changeLanguangeController, final Text text,
            final StatusBarMessageController statusBarMessageController,
            final TurnStartedController turnStartedController,
            final SelectedUnitWasChangedController selectedUnitWasChangedController) {
        this.display = Preconditions.checkNotNull(display);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        display.getNextTurnButton().setText(text.get("nextTurnButton"));
        display.getNextTurnButton().setDisable(true);

        display.getNextTurnButton().setOnAction(e -> {
            logger.debug("Next turn button was pressed");
            display.getNextTurnButton().setDisable(true);
            gameModelController.nextTurn();
        });

        display.getNextTurnButton().setOnKeyPressed(e -> {
            keyController.fireEvent(e);
        });

        display.getNextTurnButton().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("nextTurnButton.desctiption")));
        });

        display.getBox().setOnMouseEntered(e -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("rightPanel.description")));
        });

        changeLanguangeController.addListener(this::onLanguageWasChanged);
        tileWasSelectedController.addRunLaterListener(this::onFocusedTile);
        turnStartedController.addRunLaterListener(this::onTurnStarted);
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

    private void onTurnStarted(final TurnStartedEvent event) {
        logger.debug("Turn started for player {}", event.getPlayer());
        display.setOnMovePlayer(event.getPlayer());
        if (event.getPlayer().isHuman()) {
            display.getNextTurnButton().setDisable(false);
            display.refreshView(lastFocusedTileEvent);
        }
    }

    @SuppressWarnings("unused")
    private void onLanguageWasChanged(final ChangeLanguageEvent event) {
        display.getNextTurnButton().setText(text.get("nextTurnButton"));
        if (gameModelController.isModelReady()) {
            display.setOnMovePlayer(gameModelController.getModel().getCurrentPlayer());
        }
        display.refreshView(lastFocusedTileEvent);
    }

    private void onFocusedTile(final TileWasSelectedEvent event) {
        lastFocusedTileEvent = event;
        display.refreshView(event);
    }

}
