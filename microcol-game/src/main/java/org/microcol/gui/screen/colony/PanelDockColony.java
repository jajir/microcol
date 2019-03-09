package org.microcol.gui.screen.colony;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.AbstractPanelDock;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Just extends abstract dock panel and define it's source.
 */
@Singleton
public class PanelDockColony extends AbstractPanelDock {

    @Inject
    public PanelDockColony(ImageProvider imageProvider,
            final PanelColonyDockBehaviour panelColonyDockBehaviour, I18n i18n, EventBus eventBus) {
        super(imageProvider, panelColonyDockBehaviour, i18n, eventBus, Source.COLONY);
    }

}
