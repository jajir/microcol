package org.microcol.gui.dock;

import java.util.Optional;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.scene.layout.Region;

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 * <p>
 * Class should not be defined as singleton because is used in Europe port and
 * in colonies.
 * </p>
 * <p>
 * Class mix together presenter and view.
 * </p>
 */
public abstract class AbstractPanelDock implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final PanelDockCratesPresenter panelDockCratesPresenter;

    private final PanelDockView view;

    private final PanelDockBehavior panelDockBehavior;

    public AbstractPanelDock(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior,
            final AbstractPanelDockProvider abstractDockProvider) {
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        this.panelDockCratesPresenter = abstractDockProvider.createPanelDockCratesPresenter();
        this.view = new PanelDockView(imageProvider, panelDockCratesPresenter);
        view.setOnSelectedUnitWasChanged(this::onShipUnitWasSelected);
    }

    private void onShipUnitWasSelected(final Optional<UnitWithCargo> oShip) {
        if (oShip.isPresent()) {
            panelDockCratesPresenter.setCratesForShip(oShip.get());
        } else {
            panelDockCratesPresenter.closeAllCrates();
        }
    }

    @Override
    public void repaint() {
        view.cleanShips();
        if (panelDockBehavior.getUnitsInPort().isEmpty()) {
            view.setVisible(false);
        } else {
            view.setVisible(true);
            final Optional<UnitWithCargo> oSelectedShip = getSelectedShip();
            view.unselectShip();
            panelDockBehavior.getUnitsInPort().forEach(unit -> {
                view.addShip(unit);
            });
            view.selectShip(oSelectedShip);
        }
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        // intentionally do nothing
    }

    public Optional<UnitWithCargo> getSelectedShip() {
        return view.getSelectedShip();
    }

    @Override
    public Region getContent() {
        return view.getContent();
    }

    protected PanelDockCratesPresenter getPanelDockCratesPresenter() {
        return panelDockCratesPresenter;
    }

}
