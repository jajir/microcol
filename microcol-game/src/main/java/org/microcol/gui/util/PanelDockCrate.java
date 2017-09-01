package org.microcol.gui.util;

import org.microcol.gui.ImageProvider;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
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
public class PanelDockCrate extends StackPane {

	private final ImageProvider imageProvider;

	private final ImageView crateImage;

	private final ImageView cargoImage;

	private final Label labelAmount;

	private Background background;

	private CargoSlot cargoSlot;

	private final PanelDockBehavior panelDockBehavior;

	PanelDockCrate(final ImageProvider imageProvider, final PanelDockBehavior panelDockBehavior) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);

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

		labelAmount = new Label(" ");

		setOnDragDetected(this::onDragDetected);
		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);

		this.getChildren().addAll(crateImage, labelAmount);
		getStyleClass().add("cratePanel");
	}

	public void setIsClosed(final boolean isClosed) {
		if (isClosed) {
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
		if (cargoSlot.isEmpty()) {
			hideCargo();
		} else {
			if (cargoSlot.isLoadedGood()) {
				final GoodAmount goodAmount = cargoSlot.getGoods().get();
				// TODO it should appear below crate image
				labelAmount.setText(String.valueOf(goodAmount.getAmount()));
				cargoImage.setImage(imageProvider.getGoodTypeImage(goodAmount.getGoodType()));
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

	private final void onDragDropped(final DragEvent event) {
		panelDockBehavior.onDragDropped(cargoSlot, event);
	}

	private void onDragDetected(final MouseEvent event) {
		panelDockBehavior.onDragDetected(cargoSlot, event, this);
	}

	private boolean isCorrectObject(final Dragboard db) {
		return panelDockBehavior.isCorrectObject(cargoSlot, db);
	}

}
