package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Construction;

import com.google.common.eventbus.EventBus;

/**
 * Doesn't paint nothing at all.
 */
class PanelConstructionEmpty extends AbstractPanelConstruction {

    public PanelConstructionEmpty(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider, final Construction construction) {
        super(eventBus, i18n, imageProvider, construction, Point.ZERO);
    }

    @Override
    void paint() {
        // intentionally do nothing.
    }

}
