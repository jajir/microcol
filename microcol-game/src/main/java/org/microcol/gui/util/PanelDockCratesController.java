package org.microcol.gui.util;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

public final class PanelDockCratesController {

    private final PanelDockCratesView panelCratesView;

    PanelDockCratesController(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior, final I18n i18n, final EventBus eventBus,
            final Source source) {
        this.panelCratesView = new PanelDockCratesView(Preconditions.checkNotNull(imageProvider),
                panelDockBehavior, i18n, eventBus, source);
    }

    PanelDockCratesView getContent() {
        return panelCratesView;
    }

    final void setCratesForShip(final UnitWithCargo unit) {
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
