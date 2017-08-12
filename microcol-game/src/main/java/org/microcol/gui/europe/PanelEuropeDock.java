package org.microcol.gui.europe;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.CargoSlot;
import org.microcol.model.EuropePort;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
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
public class PanelEuropeDock extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelEuropeDock.class);
	
	private final static int MAX_NUMBER_OF_CRATES = 6;

	private final ImageProvider imageProvider;

	private final List<PanelCrate> crates = new ArrayList<>();

	final HBox panelShips;

	final ToggleGroup toggleGroup;

	public PanelEuropeDock(final ImageProvider imageProvider) {
		super("pristav");
		this.imageProvider = Preconditions.checkNotNull(imageProvider);

		panelShips = new HBox();
		toggleGroup = new ToggleGroup();
		toggleGroup.selectedToggleProperty().addListener((object, oldValue, newValue) -> {
			if (toggleGroup.getSelectedToggle() == null) {
				closeAllCrates();
			} else {
				setCratesForShip((Unit) toggleGroup.getSelectedToggle().getUserData());
			}
		});

		final HBox panelCrates = new HBox();
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			PanelCrate paneCrate = new PanelCrate(imageProvider);
			crates.add(paneCrate);
			panelCrates.getChildren().add(paneCrate);
		}
		VBox mainPanel = new VBox(panelShips, panelCrates);
		getContentPane().getChildren().add(mainPanel);
	}

	public void setPort(GameController gameController, final EuropePort port) {
		panelShips.getChildren().clear();
		/**
		 * Ships in port
		 */
		final List<Unit> shipsInPort = port.getShipsInPort(gameController.getModel().getCurrentPlayer());

		for (Unit unit : shipsInPort) {
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

	private void onDragDetected(final MouseEvent event) {
		if (event.getSource() instanceof ToggleButton) {
			final ToggleButton butt = (ToggleButton) event.getSource();
			final Unit unit = (Unit) butt.getUserData();
			logger.debug("Start dragging unit: " + unit);
			Preconditions.checkNotNull(unit);
			Preconditions.checkArgument(UnitType.isShip(unit.getType()), "Unit (%s) have to be ship.");
			final Dragboard db = butt.startDragAndDrop(TransferMode.MOVE);
			final ClipboardContent content = new ClipboardContent();
			content.putImage(butt.getBackground().getImages().get(0).getImage());
			content.putString(String.valueOf(unit.getId()));
			db.setContent(content);
			event.consume();
		}
	}

	private final void setCratesForShip(final Unit unit) {
		Preconditions.checkNotNull(unit);
		final int maxNumberOfCrates = unit.getType().getCargoCapacity();
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			final PanelCrate panelCrate = crates.get(i);
			if (i < maxNumberOfCrates) {
				final CargoSlot cargoSlot = unit.getHold().getSlots().get(i);
				panelCrate.showCargoSlot(cargoSlot);
			} else {
				panelCrate.setIsClosed(true);
			}
		}
	}

	private final void closeAllCrates() {
		for (final PanelCrate cratePane : crates) {
			cratePane.setIsClosed(true);
		}
	}

}
