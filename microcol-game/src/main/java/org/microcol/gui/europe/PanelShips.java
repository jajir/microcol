package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 */
public class PanelShips extends TitledPanel {

	private final boolean isShownShipsTravelingToEurope;

	private final HBox shipsContainer;

	private final ImageProvider imageProvider;

	private final Model model;

	public PanelShips(final ImageProvider imageProvider, final String title, final Model model,
			final boolean isShownShipsTravelingToEurope) {
		super(title, new Label(title));
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.model = Preconditions.checkNotNull(model);
		this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
		shipsContainer = new HBox();
		getChildren().add(shipsContainer);
		showShips();
	}

	private void showShips() {
		model.getHighSea().getUnitsTravelingTo(isShownShipsTravelingToEurope).forEach(unit -> {
			shipsContainer.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
		});
	}

}
