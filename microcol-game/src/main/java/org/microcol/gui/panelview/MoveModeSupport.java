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
			if (viewState.getMouseOverTile().isPresent()) {
				final Location target = viewState.getMouseOverTile().get();
				if (viewState.getSelectedTile().get().equals(target)) {
					/**
					 * Pointing with mouse to unit which should move.
					 */
					moveLocations = Lists.newArrayList();
				} else {
					// TODO JJ get(0) could return different ship that is really
					// moved
					final Unit movingUnit = gameController.getModel().getCurrentPlayer()
							.getUnitsAt(viewState.getSelectedTile().get()).get(0);
					final List<Unit> ships = gameController.getModel().getUnitsAt(target);
					if (ships.isEmpty()) {
						// TODO JJ step counter should be core function
						moveLocations = movingUnit.getPath(target).orElse(Collections.emptyList());
						moveMode = MoveMode.MOVE;
					} else {
						if (ships.get(0).getOwner().equals(movingUnit.getOwner())) {
							/**
							 * Move unit to another my units.
							 */
							moveLocations = movingUnit.getPath(target).orElse(Collections.emptyList());
							moveMode = MoveMode.MOVE;
						} else {
							/**
							 * User wants to fight.
							 */
							if (movingUnit.getType().canAttack()) {
								moveLocations = Lists
										.newArrayList(movingUnit.getPath(target, true).orElse(Collections.emptyList()));
								moveLocations.add(target);
								moveMode = MoveMode.FIGHT;
							} else {
								// TODO JJ change status bar, that user try to
								// attack with unit that can't attack.
								moveLocations = Collections.emptyList();
								moveMode = MoveMode.MOVE;
							}
						}
					}
				}
			} else {
				moveLocations = Collections.emptyList();
			}
		} else {
			moveLocations = Lists.newArrayList();
		}
	}

	public List<Location> getMoveLocations() {
		return moveLocations;
	}

	public MoveMode getMoveMode() {
		return moveMode;
	}

}
