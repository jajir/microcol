package org.microcol.gui.europe;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.microcol.gui.ImageProvider;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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

	private final List<Pane> crates = new ArrayList<>();

	public PanelPortPier(final ImageProvider imageProvider) {
		super("pristav");
		this.imageProvider = Preconditions.checkNotNull(imageProvider);

		//FIXME JJ get ships from API
		final List<Unit> shipsToEurope = new ArrayList<>();
		Unit ship21 = EasyMock.createMock(Unit.class);
		EasyMock.replay(ship21);
		shipsToEurope.add(ship21);

		final List<Unit> shipsToColony = new ArrayList<>();
		Unit ship11 = EasyMock.createMock(Unit.class);
		EasyMock.replay(ship11);
		shipsToColony.add(ship11);

		/**
		 * Ships in port
		 */
		final List<Unit> shipsInPort = new ArrayList<>();
		Unit ship1 = EasyMock.createMock(Unit.class);
		Unit ship2 = EasyMock.createMock(Unit.class);
		EasyMock.expect(ship1.getType()).andReturn(UnitType.FRIGATE).anyTimes();
		EasyMock.expect(ship2.getType()).andReturn(UnitType.GALLEON).anyTimes();
		EasyMock.replay(ship1, ship2);
		shipsInPort.add(ship1);
		shipsInPort.add(ship2);

		final HBox panelShips = new HBox();
		ToggleGroup toggleGroup = new ToggleGroup();
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
		toggleGroup.selectedToggleProperty().addListener((object, oldValue, newValue) -> {
			if (toggleGroup.getSelectedToggle() == null) {
				closeAllCrates();
			} else {
				setCratesForShip((Unit) toggleGroup.getSelectedToggle().getUserData());
			}
		});

		final HBox panelCrates = new HBox();
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			final ImageView imageIcon = new ImageView(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
			imageIcon.setFitWidth(70);
			imageIcon.setFitHeight(70);
			final Pane paneCrate = new Pane(imageIcon);
			paneCrate.setOnDragDropped(event -> {
				final Pane cratePane = (Pane) event.getSource();
				final ImageView crateImage = (ImageView) cratePane.getChildren().get(0);
				if (isOpen(crateImage)) {
					Dragboard db = event.getDragboard();
					logger.debug(db.getImage().toString());
					event.setDropCompleted(true);
					event.consume();
				}
			});
			paneCrate.setOnDragOver(event -> {
				final Pane cratePane = (Pane) event.getSource();
				final ImageView crateImage = (ImageView) cratePane.getChildren().get(0);
				if (isOpen(crateImage)) {
					event.acceptTransferModes(TransferMode.MOVE);
					event.consume();
				}
			});
			crates.add(paneCrate);
			panelCrates.getChildren().add(paneCrate);
		}
		VBox mainPanel = new VBox(panelShips, panelCrates);
		getContentPane().getChildren().add(mainPanel);
	}

	private final void setCratesForShip(final Unit unit) {
		Preconditions.checkNotNull(unit);
		final int maxNumberOfCrates = numberOfCrates(unit.getType());
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			final Pane cratePane = crates.get(i);
			final ImageView crateImage = (ImageView) cratePane.getChildren().get(0);
			if (i < maxNumberOfCrates) {
				// open crate
				if (!isOpen(crateImage)) {
					// change to open
					crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
				}
			} else {
				// closed crate
				if (isOpen(crateImage)) {
					// change to close
					crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
				}
			}
		}
	}

	private final void closeAllCrates() {
		for (final Pane cratePane : crates) {
			final ImageView crateImage = (ImageView) cratePane.getChildren().get(0);
			if (isOpen(crateImage)) {
				crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
			}
		}
	}

	private boolean isOpen(final ImageView crateImage) {
		return crateImage.getImage().equals(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
	}

	private int numberOfCrates(final UnitType unitType) {
		if (UnitType.FRIGATE.equals(unitType)) {
			return 2;
		} else if (UnitType.GALLEON.equals(unitType)) {
			return 5;
		} else {
			throw new IllegalArgumentException("unexpected unit type '" + unitType + "'");
		}
	}

}
