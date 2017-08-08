package org.microcol.gui.europe;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.model.CargoSlot;
import org.microcol.model.Port;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 */
public class PanelPortPier extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelPortPier.class);

	private final static int MAX_NUMBER_OF_CRATES = 6;

	private final ImageProvider imageProvider;

	private final List<PanelCrate> crates = new ArrayList<>();

	final HBox panelShips;

	final ToggleGroup toggleGroup;

	public PanelPortPier(final ImageProvider imageProvider) {
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
			// final Pane paneCrate = new Pane(imageIcon);
			// paneCrate.setOnDragDropped(event -> {
			// final Pane cratePane = (Pane) event.getSource();
			// final ImageView crateImage = (ImageView)
			// cratePane.getChildren().get(0);
			// if (isOpen(crateImage)) {
			// Dragboard db = event.getDragboard();
			// logger.debug(db.getImage().toString());
			// event.setDropCompleted(true);
			// event.consume();
			// }
			// });
			// paneCrate.setOnDragOver(event -> {
			// final Pane cratePane = (Pane) event.getSource();
			// final ImageView crateImage = (ImageView)
			// cratePane.getChildren().get(0);
			// if (isOpen(crateImage)) {
			// event.acceptTransferModes(TransferMode.MOVE);
			// event.consume();
			// }
			// });
			PanelCrate paneCrate = new PanelCrate(imageProvider);
//			final ImageView imageIcon = new ImageView(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
//			imageIcon.setFitWidth(70);
//			imageIcon.setFitHeight(70);
			crates.add(paneCrate);
			panelCrates.getChildren().add(paneCrate);
		}
		VBox mainPanel = new VBox(panelShips, panelCrates);
		getContentPane().getChildren().add(mainPanel);
	}

	public void setPort(final Port port) {
		/**
		 * Ships in port
		 */
		final List<Unit> shipsInPort = port.getShipsInPort();

		for (Unit unit : shipsInPort) {
			ToggleButton toggleButtonShip = new ToggleButton();
			BackgroundImage myBI = new BackgroundImage(imageProvider.getUnitImage(unit.getType()),
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					BackgroundSize.DEFAULT);
			toggleButtonShip.getStyleClass().add("paneShip");
			toggleButtonShip.setBackground(new Background(myBI));
			toggleButtonShip.setToggleGroup(toggleGroup);
			toggleButtonShip.setUserData(unit);
			panelShips.getChildren().add(toggleButtonShip);
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
