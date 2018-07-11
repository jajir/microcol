package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public final class GameManagerPo {

    // In model players are stored in class 'playerStore's
    private List<PlayerPo> players = new ArrayList<>();

    private boolean gameStarted;

    private String currentPlayer;

    public PlayerPo getPlayerByName(final String name) {
        Preconditions.checkState(players != null, "Players are null");
        return players.stream().filter(player -> player.getName().equals(name)).findAny()
                .orElseThrow(() -> new IllegalStateException("Invalid owner name '" + name + "'"));
    }

    /**
     * @return the players
     */
    public List<PlayerPo> getPlayers() {
        return players;
    }

    /**
     * @param players
     *            the players to set
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
     * @param gameStarted
     *            the gameStarted to set
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
     * @param currentPlayer
     *            the currentPlayer to set
     */
    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

}
