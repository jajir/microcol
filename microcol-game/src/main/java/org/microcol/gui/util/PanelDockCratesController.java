package org.microcol.gui.util;

import org.microcol.gui.image.ImageProvider;
import org.microcol.model.CargoSlot;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

public final class PanelDockCratesController {

    private final PanelDockCratesView panelCratesView;

    PanelDockCratesController(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior) {
        this.panelCratesView = new PanelDockCratesView(Preconditions.checkNotNull(imageProvider),
                panelDockBehavior);
    }

    PanelDockCratesView getPanelCratesView() {
        return panelCratesView;
    }

    final void setCratesForShip(final Unit unit) {
        Preconditions.checkNotNull(unit);
        final int maxNumberOfCrates = unit.getType().getCargoCapacity();
        for (int i = 0; i < PanelDockCratesView.MAX_NUMBER_OF_CRATES; i++) {
            final PanelDockCrate panelCrate = panelCratesView.getCrates().get(i);
            if (i < maxNumberOfCrates) {
                final CargoSlot cargoSlot = unit.getCargo().getSlots().get(i);
                panelCrate.showCargoSlot(cargoSlot);
            } else {
                panelCrate.setIsClosed(true);
            }
        }
    }

    void closeAllCrates() {
        for (final PanelDockCrate cratePane : panelCratesView.getCrates()) {
            cratePane.setIsClosed(true);
        }
    }

}
