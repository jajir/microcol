package org.microcol.gui.screen.colony;

import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * When construction panel is creating in provider this class holds all required
 * parameters.
 */
class PanelConstructionContext {

    private final ImageProvider imageProvider;
    private final EventBus eventBus;
    private final I18n i18n;
    private final Construction construction;
    private final ColonyProductionStats colonyStats;
    private final Model model;
    
    private Colony colony;

    PanelConstructionContext(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n, final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.construction = Preconditions.checkNotNull(construction);
        this.colonyStats = Preconditions.checkNotNull(colonyStats);
        this.model = Preconditions.checkNotNull(model);

    }

    ImageProvider getImageProvider() {
        return imageProvider;
    }

    EventBus getEventBus() {
        return eventBus;
    }

    I18n getI18n() {
        return i18n;
    }

    Construction getConstruction() {
        return construction;
    }

    ColonyProductionStats getColonyStats() {
        return colonyStats;
    }

    Model getModel() {
        return model;
    }

    protected Colony getColony() {
        return colony;
    }

    protected void setColony(Colony colony) {
        this.colony = colony;
    }

}
