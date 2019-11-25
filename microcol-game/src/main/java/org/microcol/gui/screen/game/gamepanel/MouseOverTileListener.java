package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When mouse change tile which is over this class set new status message.
 */
@Listener
public final class MouseOverTileListener {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final StatusBarMessageService statusBarMessageService;

    @Inject
    public MouseOverTileListener(final GameModelController gameModelController,
            final EventBus eventBus, final StatusBarMessageService statusBarMessageService) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.statusBarMessageService = Preconditions.checkNotNull(statusBarMessageService);
    }

    @Subscribe
    private void onMouseOverTileChanged(final MouseOverTileChangedEvent event) {
        if (gameModelController.getModel().getMap().isValid(event.getMouseOverTileLocaton())) {
            final TerrainType terrain = gameModelController.getModel().getMap()
                    .getTerrainTypeAt(event.getMouseOverTileLocaton());
            final Player player = gameModelController.getHumanPlayer();
            setStatusMessageForTile(player, terrain, event.getMouseOverTileLocaton());
        } else {
            eventBus.post(new StatusBarMessageEvent(Source.GAME));
        }
    }

    /**
     * When mouse is over tile method set correct status message.
     *
     * @param terrain
     *            required terrain
     * @param where
     *            required location over which is now mouse
     */
    private void setStatusMessageForTile(final Player player, final TerrainType terrain,
            final Location where) {
        final String message = statusBarMessageService.getStatusMessage(where, player, terrain,
                gameModelController.getModel().getUnitsAt(where),
                gameModelController.getModel().getColonyAt(where));
        eventBus.post(new StatusBarMessageEvent(message, Source.GAME));
    }
}
