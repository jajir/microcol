package org.microcol.gui.mainmenu;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;

public final class DeclareIndependenceEvent {

    private final Model model;

    private final Player currentPlayer;

    public DeclareIndependenceEvent(final Model model, final Player currentPlayer) {
        this.model = Preconditions.checkNotNull(model);
        this.currentPlayer = Preconditions.checkNotNull(currentPlayer);
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return the currentPlayer
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

}
