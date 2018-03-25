package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.mainmenu.BuildColonyEvent;
import org.microcol.gui.mainmenu.BuildColonyEventController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
public class BuildColonyListener {

    private final GameModelController gameModelController;

    private final SelectedUnitManager selectedUnitManagerl;

    @Inject
    public BuildColonyListener(final BuildColonyEventController buildColonyEventController,
            final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedUnitManagerl = Preconditions.checkNotNull(selectedUnitManager);
        buildColonyEventController.addListener(this::onBuildColony);
    }

    private void onBuildColony(final BuildColonyEvent event) {
        gameModelController.getModel().buildColony(event.getPlayer(), event.getUnit());
        selectedUnitManagerl.unselectUnit();
    }

}
