package org.microcol.gui.gamepanel;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.DialogColonyWasCaptured;
import org.microcol.gui.DialogUnitCantFightWarning;
import org.microcol.gui.DialogUnitCantMoveHere;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Point;
import org.microcol.gui.colony.ColonyDialog;
import org.microcol.gui.event.EndMoveController;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.ColonyWasCapturedController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.GameStartedController;
import org.microcol.gui.mainmenu.CenterViewController;
import org.microcol.gui.mainmenu.ExitGameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public final class GamePanelPresenter {

	private final Logger logger = LoggerFactory.getLogger(GamePanelPresenter.class);
	
	private final GamePreferences gamePreferences;
	
	private final GameModelController gameModelController;
	
	private final GamePanelPresenter.Display display;
	
	private Optional<Point> lastMousePosition = Optional.empty();
	
	private final SelectedTileManager selectedTileManager;
	
	private final SelectedUnitManager selectedUnitManager;
	
	private final ViewUtil viewUtil;
	
	private final StartMoveController startMoveController;
	
	private final EndMoveController endMoveController;
	
	private final ColonyDialog colonyDialog;
	
	private final Text text;
	
	private final MouseOverTileManager mouseOverTileManager;
	
	private final ModeController modeController;

	public interface Display {

		Canvas getCanvas();

		void setMoveModeOff();

		void setMoveModeOn();

		void initGame(Model model);

		Area getArea();

		void planScrollingAnimationToPoint(Point targetPoint);

		void stopTimer();

		void startMoveUnit(Unit ship);

		boolean performFightDialog(Unit unitAttacker, Unit unitDefender);

		VisibleArea getVisibleArea();

	}
	
	@Inject
	public GamePanelPresenter(final GamePanelPresenter.Display display,
			final DialogColonyWasCaptured dialogColonyWasCaptured,
			final GameModelController gameModelController,
			final KeyController keyController,
			final GameStartedController gameStartedController,
			final GamePreferences gamePreferences,
			final CenterViewController viewController,
			final ExitGameController exitGameController,
			final SelectedTileManager selectedTileManager,
			final ViewUtil viewUtil,
			final StartMoveController startMoveController,
			final EndMoveController endMoveController,
			final ColonyDialog colonyDialog, final Text text,
			final ColonyWasCapturedController colonyWasCapturedController,
			final MouseOverTileManager mouseOverTileManager,
			final ModeController modeController,
			final SelectedUnitManager selectedUnitManager,
			final GamePanelController gamePanelController) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.gamePreferences = gamePreferences;
		this.display = Preconditions.checkNotNull(display);
		this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.startMoveController = Preconditions.checkNotNull(startMoveController);
		this.endMoveController = Preconditions.checkNotNull(endMoveController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		this.text = Preconditions.checkNotNull(text);
		this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
		this.modeController = Preconditions.checkNotNull(modeController);
		this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);

		startMoveController.addListener(event -> swithToMoveMode());

		keyController.addListener(e -> {
			/**
			 * Escape
			 */
			if (KeyCode.ESCAPE == e.getCode()) {
				onKeyPressed_escape();
			}
			/**
			 * Enter
			 */
			if (KeyCode.ENTER == e.getCode()) {
				onKeyPressed_enter();
			}
			logger.debug("Pressed key: '" + e.getCode().getName() + "' has code '" + e.getCharacter() + "', modifiers '"
					+ e.getCode().isModifierKey() + "'");
		});

		display.getCanvas().setOnMousePressed(e -> {
			if (gamePanelController.isMouseEnabled()) {
				onMousePressed(e);
			}
		});
		display.getCanvas().setOnMouseReleased(e -> {
			if (gamePanelController.isMouseEnabled()) {
				onMouseReleased();
			}
		});
		display.getCanvas().setOnMouseMoved(e -> {
			onMouseMoved(e);
			lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
		});
		display.getCanvas().setOnMouseDragged(e -> {
			if (gamePanelController.isMouseEnabled()) {
				onMouseDragged(e);
				lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
			}
		});

		gameStartedController.addListener(event -> display.initGame(event.getModel()));

		viewController.addListener(event -> onCenterView());
		exitGameController.addListener(event -> display.stopTimer());
		colonyWasCapturedController.addRunLaterListener(event -> {
			dialogColonyWasCaptured.showAndWait(event);
		});
	}

	private void onCenterView() {
		logger.debug("Center view event");
		/**
		 * Here could be verification of race conditions like centering to
		 * bottom right corner of map. Luckily it's done by JViewport.
		 */
		if (selectedTileManager.getSelectedTile().isPresent()) {
			final Point p = display.getArea().getCenterToLocation(selectedTileManager.getSelectedTile().get());
			display.planScrollingAnimationToPoint(p);
		}
	}

	private boolean tryToSwitchToMoveMode(final Location currentLocation) {
		Preconditions.checkNotNull(currentLocation);
		if (selectedUnitManager.isSelectedUnitMoveable()) {
			startMoveController.fireEvent(new StartMoveEvent());
			return true;
		}
		return false;
	}

	private void tryToOpenColonyDetail(final Location currentLocation) {
		Preconditions.checkNotNull(currentLocation);
		final Optional<Colony> oColony = gameModelController.getCurrentPlayer().getColoniesAt(currentLocation);
		if (oColony.isPresent()) {
			// show colony details
			colonyDialog.showColony(oColony.get());
		}
	}

	private void swithToMoveMode() {
		Preconditions.checkArgument(selectedTileManager.getSelectedTile().isPresent(),
				"to move mode could be switched just when some tile is selected.");
		final List<Unit> units = gameModelController.getCurrentPlayer()
				.getUnitsAt(selectedTileManager.getSelectedTile().get());
		// TODO JJ Filter unit that have enough action points
		Preconditions.checkState(!units.isEmpty(), "there are some moveable units");
		final Unit unit = units.get(0);
		display.startMoveUnit(unit);
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setMoveModeOn();
	}

	private void onKeyPressed_escape() {
		if (modeController.isMoveMode()) {
			disableMoveMode();
		}
	}

	private void onKeyPressed_enter() {
		if (modeController.isMoveMode()) {
			switchToNormalMode(mouseOverTileManager.getMouseOverTile().get());
		}
	}

	private void onMousePressed(final MouseEvent e) {
		final Point pressedAt = Point.of(e.getX(), e.getY());
		final Location location = display.getArea().convertToLocation(pressedAt);
		if (gameModelController.getModel().getMap().isValid(location)) {
			logger.debug("location of mouse: " + location);
			if (modeController.isMoveMode()) {
				switchToNormalMode(location);
			} else {
				if (e.isPrimaryButtonDown()) {
					selectedTileManager.setSelectedTile(location);
					if (!tryToSwitchToMoveMode(location)) {
						tryToOpenColonyDetail(location);
					}
				}
			}
		} else {
			logger.debug("invalid mouse location: " + location);
		}
	}

	private void onMouseReleased() {
		if (modeController.isMoveMode() && lastMousePosition.isPresent()) {
			final Location loc = display.getArea().convertToLocation(lastMousePosition.get());
			switchToNormalMode(loc);
		}

	}

	private void onMouseDragged(final MouseEvent e) {
		if (lastMousePosition.isPresent()) {
			if (e.isSecondaryButtonDown()) {
				final Point currentPosition = Point.of(e.getX(), e.getY());
				final Point delta = lastMousePosition.get().substract(currentPosition);
				display.getVisibleArea().addDeltaToTopLeftPoint(delta);
			}
			if (modeController.isMoveMode()) {
				final Point currentPosition = Point.of(e.getX(), e.getY());
				final Location loc = display.getArea().convertToLocation(currentPosition);
				mouseOverTileManager.setMouseOverTile(loc);
			}
		}
	}

	private void onMouseMoved(final MouseEvent e) {
		final Point currentPosition = Point.of(e.getX(), e.getY());
		final Location loc = display.getArea().convertToLocation(currentPosition);
		mouseOverTileManager.setMouseOverTile(loc);
	}

	/**
	 * This is called when user ends action mode.
	 * 
	 * @param moveToLocation
	 *            required target location
	 */
	private void switchToNormalMode(final Location moveToLocation) {
		Preconditions.checkArgument(modeController.isMoveMode(), "switch to move mode was called from move mode");
		final Location moveFromLocation = selectedTileManager.getSelectedTile().get();
		logger.debug("Switching to normal mode, from " + moveFromLocation + " to " + moveToLocation);
		if (moveFromLocation.equals(moveToLocation)) {
			disableMoveMode();
			// it's a click? is there a colony?
			tryToOpenColonyDetail(moveToLocation);
			return;
		}
		//TODO don't call selectedTileManager.setSelectedTile
		final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
		if (movingUnit.isPossibleToCaptureColonyAt(moveToLocation)) {
			// use can capture target colony
			gameModelController.captureColonyAt(movingUnit, moveToLocation);
			disableMoveMode();
		} else if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
			// fight
			fight(movingUnit, moveToLocation);
			disableMoveMode();
		} else if (movingUnit.isPossibleToEmbarkAt(moveToLocation, true)) {
			// embark
			final Unit toLoad = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
			toLoad.getCargo().getSlots().get(0).store(movingUnit);
			// TODO JJ following code is repeated multiple times
			selectedTileManager.setSelectedTile(moveToLocation);
			disableMoveMode();
		} else if (movingUnit.isPossibleToDisembarkAt(moveToLocation, true)) {
			// try to disembark
			movingUnit.getCargo().getSlots().stream().filter(cargoSlot -> !cargoSlot.isEmpty())
					.forEach(cargoSlot -> cargoSlot.unload(moveToLocation));
			// TODO JJ following code is repeated multiple times
			selectedTileManager.setSelectedTile(moveToLocation);
			disableMoveMode();
		} else if (movingUnit.isPossibleToMoveAt(moveToLocation)) {
			// user will move
			if (movingUnit.getPath(moveToLocation).isPresent()) {
				final List<Location> path = movingUnit.getPath(moveToLocation).get();
				if (!path.isEmpty()) {
					gameModelController.performMove(movingUnit, path);
				}
				disableMoveMode();
			}
		} else if (movingUnit.isPossibleToGoToPort(moveToLocation)) {
			if (movingUnit.getPath(moveToLocation).isPresent()) {
				final List<Location> path = movingUnit.getPath(moveToLocation).get();
				if (!path.isEmpty()) {
					gameModelController.performMove(movingUnit, path);
				}
				selectedTileManager.setSelectedTile(moveToLocation);
				disableMoveMode();
			}
		} else {
			logger.error("It's not possible to determine correct operation");
			new DialogUnitCantMoveHere(viewUtil, text);
			disableMoveMode();
		}
	}
	
	private void disableMoveMode(){
		display.setMoveModeOff();
		endMoveController.fireEvent(new EndMoveEvent());
	}

	private void fight(final Unit movingUnit, final Location moveToLocation) {
		if (!movingUnit.getType().canAttack()) {
			// TODO JJ consider which tile should have focus
			selectedTileManager.setSelectedTile(moveToLocation);
			disableMoveMode();
			new DialogUnitCantFightWarning(viewUtil, text);
			return;
		}
		final Unit targetUnit = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
		if (gamePreferences.getShowFightAdvisorProperty().get()) {
			if (display.performFightDialog(movingUnit, targetUnit)) {
				// User choose to fight
				disableMoveMode();
				gameModelController.performFight(movingUnit, targetUnit);
			} else {
				// User choose to quit fight
				selectedTileManager.setSelectedTile(moveToLocation);
				disableMoveMode();
			}
		} else {
			// implicit fight
			disableMoveMode();
			gameModelController.performFight(movingUnit, targetUnit);
		}
	}


}
