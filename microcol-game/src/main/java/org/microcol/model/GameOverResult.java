package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Object holds game over result description.
 */
public class GameOverResult {

    private final String gameOverReason;

    public GameOverResult(final String gameOverReason) {
        this.gameOverReason = Preconditions.checkNotNull(gameOverReason);
    }

    /**
     * @return the gameOverReason
     */
    public String getGameOverReason() {
        return gameOverReason;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("gameOverReason", gameOverReason).toString();
    }

}
