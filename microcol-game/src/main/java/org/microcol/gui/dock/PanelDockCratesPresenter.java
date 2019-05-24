package org.microcol.gui.dock;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.CargoSlot;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.scene.layout.HBox;

/**
 * Hold list of crates.
 */
public final class PanelDockCratesPresenter implements JavaFxComponent {

    private final static int MAX_NUMBER_OF_CRATES = 6;

    private final PanelDockCratesView panelCratesView;

    private final List<PanelCratePresenter> dockCrates = new ArrayList<>();

    PanelDockCratesPresenter(final AbstractPanelDockProvider abstractPanelDockProvider) {
        for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
            dockCrates.add(abstractPanelDockProvider.createPanelDockCrate());
        }
        this.panelCratesView = new PanelDockCratesView(dockCrates);
    }

    @Override
    public HBox getContent() {
        return panelCratesView.getContent();
    }

    public void setCratesForShip(final UnitWithCargo unit) {
        Preconditions.checkNotNull(unit);
        final int maxNumberOfCrates = unit.getType().getCargoCapacity();
        for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
            final PanelCratePresenter panelCrate = dockCrates.get(i);
            if (i < maxNumberOfCrates) {
                final CargoSlot cargoSlot = unit.getCargo().getSlots().get(i);
                panelCrate.showCargoSlot(cargoSlot);
            } else {
                panelCrate.setIsClosed(true);
            }
        }
    }

    void closeAllCrates() {
        dockCrates.forEach(cratePane -> cratePane.setIsClosed(true));
    }

}
