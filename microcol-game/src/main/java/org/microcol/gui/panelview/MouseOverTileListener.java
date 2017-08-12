package org.microcol.gui.panelview;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Localized;
import org.microcol.model.Location;
import org.microcol.model.Terrain;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When mouse change tile which is over this class set new status message.
 */
public class MouseOverTileListener implements Localized {

	private final GameController gameController;

	private final StatusBarMessageController statusBarMessageController;

	private final LocalizationHelper localizationHelper;

	@Inject
	public MouseOverTileListener(final MouseOverTileChangedController mouseOverTileChangedController,
			final GameController gameController, final StatusBarMessageController statusBarMessageController,
			final LocalizationHelper localizationHelper) {
		this.gameController = Preconditions.checkNotNull(gameController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		mouseOverTileChangedController.addListener(this::onMouseOverTileChanged);

	}

	private void onMouseOverTileChanged(final MouseOverTileChangedEvent event) {
		if (gameController.getModel().getMap().isValid(event.getMouseOverTileLocaton())) {
			final Terrain terrain = gameController.getModel().getMap().getTerrainAt(event.getMouseOverTileLocaton());
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
	private void setStatusMessageForTile(final Terrain terrain, final Location where) {
		final StringBuilder buff = new StringBuilder();
		// TODO show coordinates just when debug mode is on
		buff.append("(");
		buff.append(where.getX());
		buff.append(",");
		buff.append(where.getY());
		buff.append(") ");
		buff.append(getText().get("statusBar.tile.start"));
		buff.append(" ");
		buff.append(localizationHelper.getTerrainName(terrain));
		buff.append(" ");
		buff.append(getText().get("statusBar.tile.withUnit"));
		buff.append(" ");
		gameController.getModel().getUnitsAt(where).forEach(ship -> {
			buff.append(ship.getClass().getSimpleName());
			buff.append(" ");
		});
		// TODO JJ use mouseOverTileChangedController
		statusBarMessageController.fireEvent(new StatusBarMessageEvent(buff.toString()));
	}
}
