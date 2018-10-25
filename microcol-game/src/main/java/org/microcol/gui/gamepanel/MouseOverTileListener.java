package org.microcol.gui.gamepanel;

import java.util.List;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Text;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * When mouse change tile which is over this class set new status message.
 */
public final class MouseOverTileListener {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final LocalizationHelper localizationHelper;

    private final GamePreferences gamePreferences;

    private final Text text;

    @Inject
    public MouseOverTileListener(
            final MouseOverTileChangedController mouseOverTileChangedController,
            final GameModelController gameModelController,
            final LocalizationHelper localizationHelper, final GamePreferences gamePreferences,
            final EventBus eventBus, final Text text) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
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
            buff.append(localizationHelper.getTerrainName(terrain));
            final List<Unit> units = gameModelController.getModel().getUnitsAt(where);
            if (!units.isEmpty()) {
                if (units.size() > 5) {
                    buff.append(" ");
                    buff.append(text.get("statusBar.tile.unitCount", units.size()));
                } else {
                    buff.append(" ");
                    buff.append(text.get("statusBar.tile.withUnit"));
                    buff.append(" ");
                    units.forEach(ship -> {
                        buff.append(ship.getClass().getSimpleName());
                        buff.append(" ");
                    });
                }
            }
        } else {
            buff.append(text.get("statusBar.tile.notExplored"));
        }
        eventBus.post(new StatusBarMessageEvent(buff.toString(), Source.GAME));
    }
}
