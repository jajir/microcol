package org.microcol.gui.screen.europe;

import org.microcol.gui.dock.AbstractPanelDockProvider;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PanelDockProviderEurope extends AbstractPanelDockProvider {

    @Inject
    PanelDockProviderEurope(final ImageProvider imageProvider,
            final PanelEuropeDockBehavior panelDockBehavior, final I18n i18n,
            final EventBus eventBus) {
        super(imageProvider, panelDockBehavior, i18n, eventBus, Source.EUROPE);
    }

}
