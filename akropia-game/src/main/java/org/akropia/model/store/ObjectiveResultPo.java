package org.akropia.model.store;

import java.util.List;
import org.akropia.model.FreePlayGoal;

/** Objective metadata frozen alongside score, never recomputed on reload. */
public final class ObjectiveResultPo {
    private List<FreePlayGoal> enabled = List.of();
    private List<FreePlayGoal> achieved = List.of();
    private String winner;
    private int turn;

    public List<FreePlayGoal> getEnabled() {
        return enabled;
    }

    public void setEnabled(List<FreePlayGoal> value) {
        enabled = value;
    }

    public List<FreePlayGoal> getAchieved() {
        return achieved;
    }

    public void setAchieved(List<FreePlayGoal> value) {
        achieved = value;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String value) {
        winner = value;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int value) {
        turn = value;
    }
}
