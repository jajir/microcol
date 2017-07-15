package org.microcol.gui.panelview;

import java.util.Collections;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Class helps with drawing of action path.
 * <p>
 * Class works with Locations because point could change without moving over
 * different tile. (screen scrolling)
 * </p>
 */
public class MoveModeSupport {

	private final Logger logger = LoggerFactory.getLogger(MoveModeSupport.class);

	private final ViewState viewState;

	private final GameController gameController;

	private final UnitService unitService;

	private List<Location> moveLocations;

	private MoveMode moveMode;

	/**
	 * Define what mode of move.
	 *
	 */
	public static enum MoveMode {

		MOVE(ImageProvider.IMG_ICON_STEPS_25x25, ImageProvider.IMG_ICON_STEPS_TURN_25x25),

		ANCHOR(ImageProvider.IMG_ICON_STEPS_ANCHOR_25x25, ImageProvider.IMG_ICON_STEPS_ANCHOR_TURN_25x25),

		FIGHT(ImageProvider.IMG_ICON_STEPS_FIGHT_25x25, ImageProvider.IMG_ICON_STEPS_FIGHT_TURN_25x25);

		/**
		 * Normal step image.
		 */
		private final String image;

		/**
		 * Step image indicating that here ends turn.
		 */
		private final String turnImage;

		private MoveMode(final String image, final String turnImage) {
			this.image = Preconditions.checkNotNull(image);
			this.turnImage = Preconditions.checkNotNull(turnImage);
		}

		public String getImageForStep(final boolean normalStep) {
			if (normalStep) {
				return image;
			} else {
				return turnImage;
			}
		}

		public String getImage() {
			return image;
		}

		public String getTurnImage() {
			return turnImage;
		}

	}

	@Inject
	public MoveModeSupport(final MouseOverTileChangedController mouseOverTileChangedController,
			final ViewState viewState, final GameController gameController, final UnitService unitService) {
		mouseOverTileChangedController.addListener(this::recountPath);
		this.viewState = Preconditions.checkNotNull(viewState);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.unitService = Preconditions.checkNotNull(unitService);
		moveLocations = Lists.newArrayList();
	}

	private void recountPath(final MouseOverTileChangedEvent mouseOverTileChangedEvent) {
		Preconditions.checkNotNull(mouseOverTileChangedEvent);
		logger.debug("Recounting path: " + mouseOverTileChangedEvent);
		if (viewState.isMoveMode()) {
			if (viewState.getMouseOverTile().isPresent()) {
				final Location target = viewState.getMouseOverTile().get();
				if (viewState.getSelectedTile().get().equals(target)) {
					/**
					 * Pointing with mouse to unit which should move.
					 */
					noMove();
				} else {
					processMove(target);
				}
			} else {
				noMove();
			}
		} else {
			noMove();
		}
	}

	private void processMove(final Location moveToLocation) {
		// TODO JJ moving unit should be parameter, not first unit
		final Unit movingUnit = gameController.getModel().getCurrentPlayer()
				.getUnitsAt(viewState.getSelectedTile().get()).get(0);
		if (unitService.canFight(movingUnit, moveToLocation)) {
			// fights
			moveLocations = Lists
					.newArrayList(movingUnit.getPath(moveToLocation, true).orElse(Collections.emptyList()));
			moveLocations.add(moveToLocation);
			moveMode = MoveMode.FIGHT;
		} else if (unitService.canEmbark(movingUnit, moveToLocation)) {
			// embark
			moveLocations = movingUnit.getPath(moveToLocation).orElse(Lists.newArrayList(moveToLocation));
			moveMode = MoveMode.ANCHOR;
		} else if (unitService.canDisembark(movingUnit, moveToLocation)) {
			moveLocations = movingUnit.getPath(moveToLocation).orElse(Lists.newArrayList(moveToLocation));
			moveMode = MoveMode.ANCHOR;
		} else if (unitService.canMove(movingUnit, moveToLocation)) {
			// user will move
			moveLocations = movingUnit.getPath(moveToLocation).orElse(Collections.emptyList());
			moveMode = MoveMode.MOVE;
		} else {
			noMove();
		}
	}

	private void noMove() {
		moveLocations = Lists.newArrayList();
		moveMode = MoveMode.MOVE;
	}

	public List<Location> getMoveLocations() {
		return moveLocations;
	}

	public MoveMode getMoveMode() {
		return moveMode;
	}

}
