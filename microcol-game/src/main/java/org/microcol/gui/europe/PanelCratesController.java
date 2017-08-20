package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PanelCratesController {

	private final PanelCratesView panelCratesView;

	@Inject
	PanelCratesController(final ViewUtil viewUtil, final Text text, final GameController gameController,
			final ImageProvider imageProvider, final EuropeDialog europeDialog) {
		this.panelCratesView = new PanelCratesView(viewUtil, text, gameController,
				Preconditions.checkNotNull(imageProvider), europeDialog);
	}

	PanelCratesView getPanelCratesView() {
		return panelCratesView;
	}

	final void setCratesForShip(final Unit unit) {
		Preconditions.checkNotNull(unit);
		final int maxNumberOfCrates = unit.getType().getCargoCapacity();
		for (int i = 0; i < PanelCratesView.MAX_NUMBER_OF_CRATES; i++) {
			final PanelCrate panelCrate = panelCratesView.getCrates().get(i);
			if (i < maxNumberOfCrates) {
				final CargoSlot cargoSlot = unit.getCargo().getSlots().get(i);
				panelCrate.showCargoSlot(unit, cargoSlot);
			} else {
				panelCrate.setIsClosed(true);
			}
		}
	}

	void closeAllCrates() {
		for (final PanelCrate cratePane : panelCratesView.getCrates()) {
			cratePane.setIsClosed(true);
		}
	}

}
