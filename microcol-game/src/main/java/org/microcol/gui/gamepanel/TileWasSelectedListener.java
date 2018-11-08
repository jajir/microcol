package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When tile is selected this selection is stored to model. It should be later
 * saved.
 */
@Listener
public final class TileWasSelectedListener {

    private final GameModelController gameModelController;

    @Inject
    TileWasSelectedListener(final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onTileWasSelected(final TileWasSelectedEvent event) {
        gameModelController.getModel().setFocusedField(event.getLocation());
    }

}
