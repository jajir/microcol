package org.akropia.gui.screen.colony;

import org.akropia.gui.dock.AbstractPanelDock;
import org.akropia.gui.image.ImageProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Just extends abstract dock panel and define it's source. Contains ships and
 * crates.
 */
@Singleton
class PanelDockColony extends AbstractPanelDock {

    @Inject
    public PanelDockColony(final ImageProvider imageProvider,
            final PanelColonyDockBehaviour panelColonyDockBehaviour,
            final PanelDockProviderColony panelDockProviderColony) {
        super(imageProvider, panelColonyDockBehaviour, panelDockProviderColony);
        enableHarborLayout(true);
    }

}
