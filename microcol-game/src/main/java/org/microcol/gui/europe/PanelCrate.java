package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmmount;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
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

		setOnDragDetected(this::onDragDetected);
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
			if (cargoSlot.isLoadedGood()) {
				final GoodAmmount goodAmmount = cargoSlot.getGoods().get();
				cargoImage.setImage(imageProvider.getGoodTypeImage(goodAmmount.getGoodType()));
			} else if (cargoSlot.isLoadedUnit()) {
				final Unit cargoUnit = cargoSlot.getUnit().get();
				cargoImage.setImage(imageProvider.getUnitImage(cargoUnit.getType()));
			}
		}
	}

	private void hideCargo() {
		getChildren().remove(cargoImage);
	}

	private final void onDragEntered(final DragEvent event) {
		if (isCorrectObject(event.getDragboard())) {
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
		if (isCorrectObject(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();

		ClipboardReader.make(gameController.getModel(), db).filterUnit(unit -> !UnitType.isShip(unit.getType()))
				.tryReadGood((goodAmmount, transferFrom) -> {
					cargoSlot.store(goodAmmount);
					europeDialog.repaintAfterGoodMoving();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				}).tryReadUnit((unit, transferFrom) -> {
					cargoSlot.store(unit);
					europeDialog.repaintAfterGoodMoving();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				});
	}

	private void onDragDetected(final MouseEvent event) {
		if (unit != null && cargoSlot != null) {
			if (cargoSlot.getUnit().isPresent()) {
				ClipboardWritter.make(startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage.getImage())
						.addUnit(cargoSlot.getUnit().get()).addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
						.build();
			} else if (cargoSlot.getGoods().isPresent()) {
				ClipboardWritter.make(startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage.getImage())
						.addGoodAmmount(cargoSlot.getGoods().get()).build();
			}
			event.consume();
		}
	}

	private boolean isCorrectObject(final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		if (cargoSlot != null && cargoSlot.isEmpty()) {
			return !ClipboardReader.make(gameController.getModel(), db)
					.filterUnit(unit -> !UnitType.isShip(unit.getType())).isEmpty();
		}
		return false;
	}

}
