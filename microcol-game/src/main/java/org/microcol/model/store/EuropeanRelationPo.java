package org.microcol.model.store;

import org.microcol.model.EuropeanRelationState;

public class EuropeanRelationPo {
    private String firstPlayer;
    private String secondPlayer;
    private EuropeanRelationState state;

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(final String value) {
        firstPlayer = value;
    }

    public String getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(final String value) {
        secondPlayer = value;
    }

    public EuropeanRelationState getState() {
        return state;
    }

    public void setState(final EuropeanRelationState value) {
        state = value;
    }
}
