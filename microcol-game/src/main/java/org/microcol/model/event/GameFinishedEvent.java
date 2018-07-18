package org.microcol.model.event;

import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is send when game is over. This is send just when game is over because
 * of game rules not because of players decision.
 */
public final class GameFinishedEvent extends AbstractModelEvent {

    private final GameOverResult gameOverResult;

    public GameFinishedEvent(final Model model, final GameOverResult gameOverResult) {
        super(model);
        this.gameOverResult = Preconditions.checkNotNull(gameOverResult);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

    /**
     * @return the gameOverResult
     */
    public GameOverResult getGameOverResult() {
        return gameOverResult;
    }
}
