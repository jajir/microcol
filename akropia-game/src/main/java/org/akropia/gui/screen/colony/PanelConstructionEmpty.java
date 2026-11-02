package org.akropia.gui.screen.colony;

import org.akropia.gui.Point;
import org.akropia.gui.image.ImageProvider;
import org.akropia.i18n.I18n;
import org.akropia.model.Construction;

import com.google.common.eventbus.EventBus;

/**
 * Doesn't paint nothing at all.
 */
class PanelConstructionEmpty extends AbstractPanelConstruction {

    public PanelConstructionEmpty(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider,
            final Construction construction) {
        super(eventBus, i18n, imageProvider, construction, Point.ZERO);
    }

    @Override
    void paint() {
        // intentionally do nothing.
    }

}
