package org.microcol.gui.gamepanel;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Text;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When mouse change tile which is over this class set new status message.
 */
public class MouseOverTileListener {

    private final GameModelController gameModelController;

    private final StatusBarMessageController statusBarMessageController;

    private final LocalizationHelper localizationHelper;

    private final GamePreferences gamePreferences;

    private final Text text;

    @Inject
    public MouseOverTileListener(
            final MouseOverTileChangedController mouseOverTileChangedController,
            final GameModelController gameModelController,
            final StatusBarMessageController statusBarMessageController,
            final LocalizationHelper localizationHelper, final GamePreferences gamePreferences,
            final Text text) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.text = Preconditions.checkNotNull(text);
        mouseOverTileChangedController.addListener(this::onMouseOverTileChanged);

    }

    private void onMouseOverTileChanged(final MouseOverTileChangedEvent event) {
        if (gameModelController.getModel().getMap().isValid(event.getMouseOverTileLocaton())) {
            final TerrainType terrain = gameModelController.getModel().getMap()
                    .getTerrainTypeAt(event.getMouseOverTileLocaton());
            final Player player = gameModelController.getCurrentPlayer();
            setStatusMessageForTile(player, terrain, event.getMouseOverTileLocaton());
        } else {
            statusBarMessageController.fireEvent(new StatusBarMessageEvent());
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
        final StringBuilder buff = new StringBuilder();
        if (gamePreferences.isDevelopment()) {
            buff.append("(");
            buff.append(where.getX());
            buff.append(",");
            buff.append(where.getY());
            buff.append(") ");
        }
        if (player.isVisible(where)) {
            buff.append(text.get("statusBar.tile.start"));
            buff.append(" ");
            buff.append(localizationHelper.getTerrainName(terrain));
            buff.append(" ");
            buff.append(text.get("statusBar.tile.withUnit"));
            buff.append(" ");
            gameModelController.getModel().getUnitsAt(where).forEach(ship -> {
                buff.append(ship.getClass().getSimpleName());
                buff.append(" ");
            });
        } else {
            buff.append(text.get("statusBar.tile.notExplored"));
        }
        statusBarMessageController.fireEvent(new StatusBarMessageEvent(buff.toString()));
    }
}
