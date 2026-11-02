package org.akropia.gui.screen.colony;

import org.akropia.gui.dock.AbstractPanelDockProvider;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class PanelDockProviderColony extends AbstractPanelDockProvider {

    @Inject
    PanelDockProviderColony(final ImageProvider imageProvider,
            final PanelColonyDockBehaviour panelDockBehavior, final I18n i18n,
            final EventBus eventBus) {
        super(imageProvider, panelDockBehavior, i18n, eventBus, Source.COLONY);
    }

}
