package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.BuildColonyEvent;
import org.microcol.gui.mainmenu.BuildColonyEventController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
public final class BuildColonyListener {

    private final GameModelController gameModelController;

    @Inject
    public BuildColonyListener(final BuildColonyEventController buildColonyEventController,
            final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        buildColonyEventController.addListener(this::onBuildColony);
    }

    private void onBuildColony(final BuildColonyEvent event) {
        gameModelController.getModel().buildColony(event.getPlayer(), event.getUnit());
    }

}
