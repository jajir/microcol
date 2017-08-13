package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.CargoSlot;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Container represents one open or close crate.
 */
public class PanelCrate extends StackPane {

	private final Logger logger = LoggerFactory.getLogger(PanelCrate.class);

	private final ImageProvider imageProvider;

	private final ImageView crateImage;

	private final ImageView cargoImage;

	private Background background;

	private final GameController gameController;

	private Unit unit;

	private CargoSlot cargoSlot;

	private final EuropeDialog europeDialog;

	PanelCrate(final GameController gameController, final ImageProvider imageProvider,
			final EuropeDialog europeDialog) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.europeDialog = Preconditions.checkNotNull(europeDialog);

		crateImage = new ImageView();
		crateImage.getStyleClass().add("crate");
		crateImage.setFitWidth(40);
		crateImage.setFitHeight(40);
		crateImage.setPreserveRatio(true);

		cargoImage = new ImageView();
		cargoImage.getStyleClass().add("cargo");
		cargoImage.setFitWidth(35);
		cargoImage.setFitHeight(35);
		cargoImage.setPreserveRatio(true);
		cargoImage.setOnDragDetected(this::onDragDetected);

		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);

		this.getChildren().addAll(crateImage);
		getStyleClass().add("cratePanel");
	}

	public void setIsClosed(final boolean isClosed) {
		if (isClosed) {
			unit = null;
			cargoSlot = null;
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
			hideCargo();
		} else {
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
			if (!getChildren().contains(cargoImage)) {
				getChildren().add(cargoImage);
			}
		}
	}

	public void showCargoSlot(final Unit unit, final CargoSlot cargoSlot) {
		setIsClosed(false);
		this.cargoSlot = cargoSlot;
		this.unit = unit;
		if (cargoSlot.isEmpty()) {
			hideCargo();
		} else {
			final Unit cargoUnit = cargoSlot.getUnit().get();
			cargoImage.setImage(imageProvider.getUnitImage(cargoUnit.getType()));
		}
	}

	private void hideCargo() {
		getChildren().remove(cargoImage);
	}

	private final void onDragEntered(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		setBackground(background);
		background = null;
	}

	private final void onDragOver(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		if (db.hasString()) {
			logger.debug("Drag over unit id '" + db.getString() + "'.");
			final Unit draggedUnit = gameController.getModel().getUnitById(Integer.valueOf(db.getString()));
			cargoSlot.store(draggedUnit);
			europeDialog.repaintAfterGoodMoving();
			/*
			 * let the source know whether the string was successfully
			 * transferred and used
			 */
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		} else {
			return;
		}
	}

	private void onDragDetected(final MouseEvent event) {
		if (unit != null && cargoSlot != null) {
			final Unit cargoUnit = cargoSlot.getUnit().get();
			final ClipboardContent content = new ClipboardContent();
			content.putImage(cargoImage.getImage());
			content.putString(String.valueOf(cargoUnit.getId()));
			final Dragboard db = startDragAndDrop(TransferMode.MOVE);
			db.setContent(content);
			event.consume();
		}
	}

	private boolean isItCorrectObject(final Dragboard db) {
		if (db.hasString()) {
			logger.debug("Drag over unit id '" + db.getString() + "'.");
			if (unit == null || cargoSlot == null) {
				return false;
			} else {
				final Unit unit = gameController.getModel().getUnitById(Integer.valueOf(db.getString()));
				return !UnitType.isShip(unit.getType()) && cargoSlot.isEmpty();
			}
		} else {
			return false;
		}
	}

}
