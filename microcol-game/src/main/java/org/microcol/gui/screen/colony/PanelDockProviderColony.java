package org.microcol.gui.screen.colony;

import org.microcol.gui.dock.AbstractPanelDockProvider;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

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
