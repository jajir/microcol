package org.microcol.gui.gamepanel;

import java.util.Collections;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
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

	private final SelectedTileManager selectedTileManager;

	private final MouseOverTileManager mouseOverTileManager;
	
	private final SelectedUnitManager selectedUnitManager;
	
	private final ModeController modeController;

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
			final StartMoveController startMoveController,
			final SelectedTileManager selectedTileManager,
			final MouseOverTileManager mouseOverTileManager,
			final SelectedUnitManager selectedUnitManager,
			final ModeController modeController) {
		mouseOverTileChangedController.addListener(this::onMouseOverTileChanged);
		startMoveController.addListener(this::onStartMove);
		this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
		this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
		this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
		this.modeController = Preconditions.checkNotNull(modeController);
		moveLocations = Lists.newArrayList();
	}

	@SuppressWarnings("unused")
	private void onStartMove(final StartMoveEvent event) {
		recountPath();
	}

	private void onMouseOverTileChanged(final MouseOverTileChangedEvent mouseOverTileChangedEvent) {
		Preconditions.checkNotNull(mouseOverTileChangedEvent);
		logger.debug("Recounting path: " + mouseOverTileChangedEvent);
		if (modeController.isMoveMode()) {
			recountPath();
		} else {
			noMove();
		}
	}

	private void recountPath() {
		if (mouseOverTileManager.getMouseOverTile().isPresent()) {
			final Location target = mouseOverTileManager.getMouseOverTile().get();
			if (selectedTileManager.getSelectedTile().get().equals(target)) {
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
	}

	private void processMove(final Location moveToLocation) {
		// TODO JJ moving unit should be parameter, not first unit
		final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
		if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
			// fights
			moveLocations = Lists
					.newArrayList(movingUnit.getPath(moveToLocation, true).orElse(Collections.emptyList()));
			moveLocations.add(moveToLocation);
			moveMode = MoveMode.FIGHT;
		} else if (movingUnit.isPossibleToEmbarkAt(moveToLocation, true)) {
			// embark
			moveLocations = movingUnit.getPath(moveToLocation).orElse(Lists.newArrayList(moveToLocation));
			moveMode = MoveMode.ANCHOR;
		} else if (movingUnit.isPossibleToDisembarkAt(moveToLocation, true)) {
			moveLocations = movingUnit.getPath(moveToLocation).orElse(Lists.newArrayList(moveToLocation));
			moveMode = MoveMode.ANCHOR;
		} else if (movingUnit.isPossibleToMoveAt(moveToLocation)) {
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
