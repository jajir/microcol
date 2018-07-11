package org.microcol.model;

import org.microcol.model.store.TurnPlayerStatisticsPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Record statistics for concrete player for specific turn.
 */
public final class TurnPlayerStatistics {

    private final Player player;

    private final int turnNo;

    private int econonyScore;

    private int militaryScore;

    private int gold;

    private int score;

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
        out.setGold(turnPlayerStatisticsPo.getGold());
        out.setScore(turnPlayerStatisticsPo.getScore());
        return out;
    }

    TurnPlayerStatisticsPo save() {
        final TurnPlayerStatisticsPo out = new TurnPlayerStatisticsPo();
        out.setPlayer(player.getName());
        out.setTurnNo(turnNo);
        out.setMilitaryScore(militaryScore);
        out.setEcononyScore(econonyScore);
        out.setGold(gold);
        out.setScore(score);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("turnNo", turnNo).add("playerNames", player.getName())
                .add("gold", gold).add("militaryScore", militaryScore)
                .add("econonyScore", econonyScore).add("score", score).toString();
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

    /**
     * @return the gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * @param gold
     *            the gold to set
     */
    public void setGold(int gold) {
        this.gold = gold;
    }

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * @param score
     *            the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

}
