package org.microcol.gui.panelview;

import org.microcol.gui.GamePreferences;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Localized;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When mouse change tile which is over this class set new status message.
 */
public class MouseOverTileListener implements Localized {

	private final GameModelController gameModelController;

	private final StatusBarMessageController statusBarMessageController;

	private final LocalizationHelper localizationHelper;
	
	private final GamePreferences gamePreferences;

	@Inject
	public MouseOverTileListener(final MouseOverTileChangedController mouseOverTileChangedController,
			final GameModelController gameModelController, final StatusBarMessageController statusBarMessageController,
			final LocalizationHelper localizationHelper, final GamePreferences gamePreferences) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		mouseOverTileChangedController.addListener(this::onMouseOverTileChanged);

	}

	private void onMouseOverTileChanged(final MouseOverTileChangedEvent event) {
		if (gameModelController.getModel().getMap().isValid(event.getMouseOverTileLocaton())) {
			final TerrainType terrain = gameModelController.getModel().getMap().getTerrainTypeAt(event.getMouseOverTileLocaton());
			setStatusMessageForTile(terrain, event.getMouseOverTileLocaton());
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
	private void setStatusMessageForTile(final TerrainType terrain, final Location where) {
		final StringBuilder buff = new StringBuilder();
		if (gamePreferences.isDevelopment()) {
			buff.append("(");
			buff.append(where.getX());
			buff.append(",");
			buff.append(where.getY());
			buff.append(") ");
		}
		buff.append(getText().get("statusBar.tile.start"));
		buff.append(" ");
		buff.append(localizationHelper.getTerrainName(terrain));
		buff.append(" ");
		buff.append(getText().get("statusBar.tile.withUnit"));
		buff.append(" ");
		gameModelController.getModel().getUnitsAt(where).forEach(ship -> {
			buff.append(ship.getClass().getSimpleName());
			buff.append(" ");
		});
		statusBarMessageController.fireEvent(new StatusBarMessageEvent(buff.toString()));
	}
}
