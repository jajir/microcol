package org.akropia.model.store;

import java.util.Map;

import org.akropia.model.EuropeanNation;
import org.akropia.model.NativeNation;

/** Saved national favour and the current forty-turn friendship period. */
public final class NativeFriendshipPo {
    private NativeNation nation;
    private Map<EuropeanNation, Integer> favour;
    private EuropeanNation friend;
    private int elapsed;
    private int arrival;

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation value) {
        nation = value;
    }

    public Map<EuropeanNation, Integer> getFavour() {
        return favour;
    }

    public void setFavour(final Map<EuropeanNation, Integer> value) {
        favour = value;
    }

    public EuropeanNation getFriend() {
        return friend;
    }

    public void setFriend(final EuropeanNation value) {
        friend = value;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(final int value) {
        elapsed = value;
    }

    public int getArrival() {
        return arrival;
    }

    public void setArrival(final int value) {
        arrival = value;
    }
}
