package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When tile is selected this selection is stored to model. It should be later
 * saved.
 */
public final class TileWasSelectedListener {

    private final GameModelController gameModelController;

    @Inject
    TileWasSelectedListener(final TileWasSelectedController tileWasSelectedController,
            final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        tileWasSelectedController.addListener(this::onTileWasSelected);
    }

    private void onTileWasSelected(final TileWasSelectedEvent event) {
        gameModelController.getModel().setFocusedField(event.getLocation());
    }

}
