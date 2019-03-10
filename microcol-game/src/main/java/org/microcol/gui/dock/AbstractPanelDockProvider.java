package org.microcol.gui.dock;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

public abstract class AbstractPanelDockProvider {

    private final ImageProvider imageProvider;
    private final PanelDockBehavior panelDockBehavior;
    private final I18n i18n;
    private final EventBus eventBus;
    private final Source source;

    public AbstractPanelDockProvider(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior, final I18n i18n, final EventBus eventBus,
            final Source source) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
    }

    PanelCratePresenter createPanelDockCrate() {
        PanelCrateView panelDockCrateView = new PanelCrateView(imageProvider);
        return new PanelCratePresenter(panelDockBehavior, i18n, eventBus, source, panelDockCrateView);
    }

    PanelDockCratesPresenter createPanelDockCratesPresenter() {
        return new PanelDockCratesPresenter(this);
    }

}
