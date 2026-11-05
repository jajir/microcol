package org.akropia.gui.screen.game.components;

import org.akropia.gui.Loc;
import org.akropia.gui.event.RefreshRightPanelEvent;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitWasChangedEvent;
import org.akropia.gui.screen.game.gamepanel.TileWasSelectedEvent;
import org.akropia.gui.util.Listener;
import org.akropia.i18n.I18n;
import org.akropia.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class RightPanelPresenter {

    private final Logger logger = LoggerFactory
            .getLogger(RightPanelPresenter.class);

    private final GameModelController gameModelController;

    private final RightPanelView display;

    private TileWasSelectedEvent lastFocusedTileEvent;

    @Inject
    RightPanelPresenter(final RightPanelView display,
            final GameModelController gameModelController, final I18n i18n,
            final EventBus eventBus) {
        this.display = Preconditions.checkNotNull(display);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);

        display.getContent().setOnMouseEntered(e -> {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Loc.rightPanel_description), Source.GAME));
        });

    }

    @Subscribe
    private void onAssignmentChanged(
            final org.akropia.model.event.ActionWasStartedEvent event) {
        display.refreshView(lastFocusedTileEvent);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onRefreshRightPanelEvent(final RefreshRightPanelEvent event) {
        display.refreshView(lastFocusedTileEvent);
    }

    @Subscribe
    private void onSelectedUnitWasChanged(
            final SelectedUnitWasChangedEvent event) {
        if (lastFocusedTileEvent == null) {
            if (event.getSelectedUnit().isPresent()) {
                display.refreshView(
                        event.getSelectedUnit().get().getLocation());
            } else {
                display.cleanView();
            }
        } else {
            display.refreshView(lastFocusedTileEvent);
        }
    }

    @Subscribe
    private void onUnitAttacked(
            final org.akropia.model.event.UnitAttackedEvent event) {
        display.refreshView(lastFocusedTileEvent);
    }

    @Subscribe
    private void onUnitMoved(
            final org.akropia.model.event.UnitMovedStepFinishedEvent event) {
        display.refreshView(lastFocusedTileEvent);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        logger.debug("Turn started for player {}", event.getPlayer());
        display.setOnMovePlayer(event.getPlayer());
        if (event.getPlayer().isHuman()) {
            if (lastFocusedTileEvent != null) {
                display.refreshView(lastFocusedTileEvent);
            }
        }
    }

    @SuppressWarnings("unused")
    public void updateLanguage(final I18n i18n) {
        if (gameModelController.isGameModelReady()) {
            display.setOnMovePlayer(
                    gameModelController.getModel().getCurrentPlayer());
        }
        if (lastFocusedTileEvent != null
                && gameModelController.isGameModelReady()) {
            display.refreshView(lastFocusedTileEvent);
        }
    }

    @Subscribe
    private void onFocusedTile(final TileWasSelectedEvent event) {
        lastFocusedTileEvent = event;
        display.refreshView(event);
    }

    /**
     * Refresh could be replace be event than game screen is about to open.
     * Event is not goods because refresh should come as first before showing
     * whole screen.
     */
    public void refresh() {
        display.refreshView(lastFocusedTileEvent);

    }

}
