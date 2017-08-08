package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.model.CargoSlot;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Container represents
 */
public class PanelCrate extends StackPane {

	private final ImageProvider imageProvider;

	private final ImageView crateImage;

	private final ImageView cargoImage;

	PanelCrate(final ImageProvider imageProvider) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);

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

		this.getChildren().addAll(crateImage);
		getStyleClass().add("cratePanel");
	}

	public void setIsClosed(final boolean isClosed) {
		if (isClosed) {
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
			hideCargo();
		} else {
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
			if (!getChildren().contains(cargoImage)) {
				getChildren().add(cargoImage);
			}
		}
	}

	public void showCargoSlot(final CargoSlot cargoSlot) {
		setIsClosed(false);
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

}
