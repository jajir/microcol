package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class DeclareIndependenceListener {

    private final GameModelController gameModelController;

    @Inject
    public DeclareIndependenceListener(final GameModelController gameModelController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);

    }

    @Subscribe
    @SuppressWarnings("ucd")
    public void onEvent(@SuppressWarnings("unused") final DeclareIndependenceEvent event) {
        final Player currentPlayer = gameModelController.getHumanPlayer();
        currentPlayer.declareIndependence();
    }

}
