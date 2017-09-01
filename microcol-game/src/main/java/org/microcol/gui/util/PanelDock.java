package org.microcol.gui.util;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 */
public class PanelDock extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelDock.class);

	private final ImageProvider imageProvider;

	private final PanelDockCratesController panelCratesController;

	private final HBox panelShips;

	private final ToggleGroup toggleGroup;

	private final GameController gameController;

	public PanelDock(final GameController gameController, final ImageProvider imageProvider,
			final PanelDockBehavior panelDockBehavior) {
		super("pristav");
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		panelCratesController = new PanelDockCratesController(imageProvider, panelDockBehavior);

		panelShips = new HBox();
		toggleGroup = new ToggleGroup();
		toggleGroup.selectedToggleProperty().addListener((object, oldValue, newValue) -> {
			if (toggleGroup.getSelectedToggle() == null) {
				panelCratesController.closeAllCrates();
			} else {
				panelCratesController.setCratesForShip(getSelectedShip().get());
			}
		});

		VBox mainPanel = new VBox(panelShips, panelCratesController.getPanelCratesView());
		getContentPane().getChildren().add(mainPanel);
	}

	public void repaint() {
		panelShips.getChildren().clear();
		for (Unit unit : getUnitsInPort()) {
			ToggleButton toggleButtonShip = new ToggleButton();
			BackgroundImage myBI = new BackgroundImage(imageProvider.getUnitImage(unit.getType()),
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					BackgroundSize.DEFAULT);
			toggleButtonShip.getStyleClass().add("paneShip");
			toggleButtonShip.setBackground(new Background(myBI));
			toggleButtonShip.setToggleGroup(toggleGroup);
			toggleButtonShip.setUserData(unit);
			toggleButtonShip.setOnDragDetected(this::onDragDetected);
			panelShips.getChildren().add(toggleButtonShip);
		}
	}

	public void repaintCurrectShipsCrates() {
		panelCratesController.setCratesForShip(getSelectedShip().get());
	}

	private Optional<Unit> getSelectedShip() {
		return Optional.of((Unit) toggleGroup.getSelectedToggle().getUserData());
	}

	/**
	 * Move to new behavior interface
	 */

	private void onDragDetected(final MouseEvent event) {
		if (event.getSource() instanceof ToggleButton) {
			final ToggleButton butt = (ToggleButton) event.getSource();
			final Unit unit = (Unit) butt.getUserData();
			logger.debug("Start dragging unit: " + unit);
			Preconditions.checkNotNull(unit);
			Preconditions.checkArgument(UnitType.isShip(unit.getType()), "Unit (%s) have to be ship.");
			ClipboardWritter.make(butt.startDragAndDrop(TransferMode.MOVE))
					.addImage(butt.getBackground().getImages().get(0).getImage()).addUnit(unit).build();
			event.consume();
		}
	}

	private List<Unit> getUnitsInPort() {
		return gameController.getModel().getEurope().getPort()
				.getShipsInPort(gameController.getModel().getCurrentPlayer());
	}
}
