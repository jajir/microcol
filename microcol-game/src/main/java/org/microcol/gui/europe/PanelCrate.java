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
		cargoImage = new ImageView();
		this.getChildren().addAll(crateImage, cargoImage);
	}

	public void setIsClosed(final boolean isClosed) {
		if (isClosed) {
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
		} else {
			crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
		}
	}

	public void showCargoSlot(final CargoSlot cargoSlot) {
		setIsClosed(false);
		if (!cargoSlot.isEmpty()) {
			final Unit cargoUnit = cargoSlot.getUnit().get();
			crateImage.setImage(imageProvider.getUnitImage(cargoUnit.getType()));
		}
	}

}
