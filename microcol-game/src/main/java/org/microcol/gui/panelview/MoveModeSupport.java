package org.microcol.gui.panelview;

import java.util.Collections;
import java.util.List;

import org.microcol.gui.event.GameController;
import org.microcol.model.Location;
import org.microcol.model.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Class helps with drawing of moving path.
 * <p>
 * Class works with Locations because point could change without moving over
 * different tile. (screen scrolling)
 * </p>
 */
public class MoveModeSupport {

	private final Logger logger = LoggerFactory.getLogger(MoveModeSupport.class);

	private final ViewState viewState;

	private final GameController gameController;

	private List<Location> moveLocations;

	@Inject
	public MoveModeSupport(final MouseOverTileChangedController mouseOverTileChangedController,
			final ViewState viewState, final GameController gameController) {
		mouseOverTileChangedController.addListener(this::recountPath);
		this.viewState = Preconditions.checkNotNull(viewState);
		this.gameController = Preconditions.checkNotNull(gameController);
		moveLocations = Lists.newArrayList();
	}

	private void recountPath(final MouseOverTileChangedEvent mouseOverTileChangedEvent) {
		Preconditions.checkNotNull(mouseOverTileChangedEvent);
		logger.debug("Recounting path: " + mouseOverTileChangedEvent);
		if (viewState.isMoveMode()) {
			if (viewState.getSelectedTile().get().equals(viewState.getMouseOverTile().get())) {
				moveLocations = Lists.newArrayList();
			} else {
				// TODO JJ get(0) could return different ship that is really
				// moved
				final Ship unit = gameController.getModel().getCurrentPlayer()
						.getShipsAt(viewState.getSelectedTile().get()).get(0);
				// TODO JJ step counter should be core function
				moveLocations = unit.getPath(viewState.getMouseOverTile().get()).orElse(Collections.emptyList());
			}
		} else {
			moveLocations = Lists.newArrayList();
		}
	}

	public List<Location> getMoveLocations() {
		return moveLocations;
	}

}
