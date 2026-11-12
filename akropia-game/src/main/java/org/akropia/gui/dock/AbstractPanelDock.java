package org.akropia.gui.dock;

import java.util.Optional;

import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.Repaintable;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import org.akropia.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.scene.layout.Region;
import javafx.scene.control.Button;

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
public abstract class AbstractPanelDock
        implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final PanelDockCratesPresenter panelDockCratesPresenter;

    private final PanelDockView view;

    private final PanelDockBehavior panelDockBehavior;

    public AbstractPanelDock(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior,
            final AbstractPanelDockProvider abstractDockProvider) {
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        this.panelDockCratesPresenter = abstractDockProvider
                .createPanelDockCratesPresenter();
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
        final Optional<UnitWithCargo> oSelectedShip = getSelectedShip();
        view.cleanShips();
        if (panelDockBehavior.getUnitsInPort().isEmpty()) {
            view.setVisible(false);
        } else {
            view.setVisible(true);
            panelDockBehavior.getUnitsInPort().forEach(unit -> {
                view.addShip(unit);
            });
            view.selectShip(oSelectedShip);
        }
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        view.updateLanguage(i18n);
    }

    protected void enableHarborLayout(final boolean includeWagons) {
        view.enableHarborLayout(includeWagons);
    }

    public void setHarborAction(final Button action) {
        view.setHarborAction(action);
    }

    public void selectShip(final UnitWithCargo ship) {
        view.selectShip(Optional.of(ship));
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
