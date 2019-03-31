package org.microcol.model.campaign;

import java.util.function.Function;

import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

/**
 * It's game over evaluator that allows stop game from outside of model. It
 * allows to end game from reasons not related to model itself. It's used to
 * stop game when all goals are fulfilled.
 */
public class ExternalGameOver implements Function<Model, GameOverResult> {

    private final String gameOverReason;

    private boolean stopGame = false;

    ExternalGameOver(final String gameOverReason) {
        this.gameOverReason = Preconditions.checkNotNull(gameOverReason);
    }

    @Override
    public GameOverResult apply(final Model model) {
        if (stopGame) {
            return new GameOverResult(gameOverReason);
        } else {
            return null;
        }
    }

    public void setStopGame() {
        this.stopGame = true;
    }

}
