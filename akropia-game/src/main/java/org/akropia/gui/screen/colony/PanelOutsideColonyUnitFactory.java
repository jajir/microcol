package org.akropia.gui.screen.colony;

import org.akropia.gui.dialog.DialogOrdainMissionary;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.PaintService;
import org.akropia.model.Colony;
import org.akropia.model.Unit;
import org.akropia.i18n.I18n;

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

    private final DialogEquipment equipmentDialog;

    private final DialogOrdainMissionary ordinationDialog;

    private final I18n i18n;

    @Inject
    PanelOutsideColonyUnitFactory(final ImageProvider imageProvider,
            final EventBus eventBus, final PaintService paintService,
            final DialogEquipment equipmentDialog, final I18n i18n,
            final DialogOrdainMissionary ordinationDialog) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.equipmentDialog = equipmentDialog;
        this.i18n = i18n;
        this.ordinationDialog = Preconditions.checkNotNull(ordinationDialog);
    }

    PanelOutsideColonyUnit make(final Unit unit, final Colony colony) {
        return new PanelOutsideColonyUnit(imageProvider, eventBus, paintService,
                colony, unit, equipmentDialog, i18n, ordinationDialog);
    }

}
