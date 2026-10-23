package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

public final class GameManagerPo {

    // In model players are stored in class 'playerStore's
    private List<PlayerPo> players = new ArrayList<>();

    private boolean gameStarted;

    private boolean gameFinished;

    /** Reason of the terminal result; null when none was recorded. */
    private String gameOverReason;

    /** Frozen free-play result (R-0193); null while no result is frozen. */
    private GameResultPo gameResult;

    public GameResultPo getGameResult() {
        return gameResult;
    }

    public void setGameResult(final GameResultPo gameResult) {
        this.gameResult = gameResult;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(final boolean gameFinished) {
        this.gameFinished = gameFinished;
    }

    public String getGameOverReason() {
        return gameOverReason;
    }

    public void setGameOverReason(final String gameOverReason) {
        this.gameOverReason = gameOverReason;
    }

    private String currentPlayer;

    /**
     * @return the players
     */
    public List<PlayerPo> getPlayers() {
        return players;
    }

    /**
     * @param players the players to set
     */
    public void setPlayers(List<PlayerPo> players) {
        this.players = players;
    }

    /**
     * @return the gameStarted
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * @param gameStarted the gameStarted to set
     */
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    /**
     * @return the currentPlayer
     */
    public String getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * @param currentPlayer the currentPlayer to set
     */
    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

}
