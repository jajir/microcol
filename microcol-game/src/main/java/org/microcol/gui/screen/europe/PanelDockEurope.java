package org.microcol.gui.screen.europe;

import org.microcol.gui.dock.AbstractPanelDock;
import org.microcol.gui.image.ImageProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Just extends abstract dock panel and define it's source.
 */
@Singleton
public class PanelDockEurope extends AbstractPanelDock {

    @Inject
    public PanelDockEurope(final ImageProvider imageProvider,
            final PanelEuropeDockBehavior panelEuropeDockBehavior,
            final PanelDockProviderEurope panelDockProviderEurope) {
        super(imageProvider, panelEuropeDockBehavior, panelDockProviderEurope);
    }

    public void repaintCurrectShipsCrates() {
        getSelectedShip().ifPresent(unit -> getPanelDockCratesPresenter().setCratesForShip(unit));
    }

}
