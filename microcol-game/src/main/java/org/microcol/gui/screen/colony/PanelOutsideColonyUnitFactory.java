package org.microcol.gui.screen.colony;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Colony;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Create new instance of {@link PanelOutsideColonyUnit}.
 */
@Singleton
class PanelOutsideColonyUnitFactory {

    private final ImageProvider imageProvider;

    private final EventBus eventBus;

    private final PaintService paintService;

    @Inject
    PanelOutsideColonyUnitFactory(final ImageProvider imageProvider, final EventBus eventBus,
            final PaintService paintService) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.paintService = Preconditions.checkNotNull(paintService);
    }

    PanelOutsideColonyUnit make(final Unit unit, final Colony colony) {
        return new PanelOutsideColonyUnit(imageProvider, eventBus, paintService, colony, unit);
    }

}
