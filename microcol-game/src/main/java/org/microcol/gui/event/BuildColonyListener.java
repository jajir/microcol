package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.BuildColonyEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
@Listener
public final class BuildColonyListener {

    private final GameModelController gameModelController;

    @Inject
    public BuildColonyListener(final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onBuildColony(final BuildColonyEvent event) {
        gameModelController.getModel().buildColony(event.getPlayer(), event.getUnit());
    }

}
