package org.microcol.model;

import org.microcol.model.store.TurnPlayerStatisticsPo;

import com.google.common.base.Preconditions;

/**
 * Record statistics for concrete player for specific turn.
 */
public class TurnPlayerStatistics {

    private final Player player;

    private final int turnNo;

    private int econonyScore;

    private int militaryScore;

    TurnPlayerStatistics(final Player player, final int turnNo) {
        this.player = Preconditions.checkNotNull(player);
        this.turnNo = turnNo;
    }

    public static TurnPlayerStatistics make(TurnPlayerStatisticsPo turnPlayerStatisticsPo,
            final PlayerStore playerStore) {
        final TurnPlayerStatistics out = new TurnPlayerStatistics(
                playerStore.getPlayerByName(turnPlayerStatisticsPo.getPlayer()),
                turnPlayerStatisticsPo.getTurnNo());
        out.setEcononyScore(turnPlayerStatisticsPo.getEcononyScore());
        out.setMilitaryScore(turnPlayerStatisticsPo.getMilitaryScore());
        return out;
    }

    TurnPlayerStatisticsPo save() {
        final TurnPlayerStatisticsPo out = new TurnPlayerStatisticsPo();
        out.setPlayer(player.getName());
        out.setTurnNo(turnNo);
        out.setMilitaryScore(militaryScore);
        out.setEcononyScore(econonyScore);
        return out;
    }

    /**
     * @return the econonyScore
     */
    public int getEcononyScore() {
        return econonyScore;
    }

    /**
     * @param econonyScore
     *            the econonyScore to set
     */
    public void setEcononyScore(int econonyScore) {
        this.econonyScore = econonyScore;
    }

    /**
     * @return the militaryScore
     */
    public int getMilitaryScore() {
        return militaryScore;
    }

    /**
     * @param militaryScore
     *            the militaryScore to set
     */
    public void setMilitaryScore(int militaryScore) {
        this.militaryScore = militaryScore;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the turnNo
     */
    public int getTurnNo() {
        return turnNo;
    }

}
