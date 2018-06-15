package org.microcol.model.store;

public class TurnPlayerStatisticsPo {

    private String player;

    private int turnNo;

    private int econonyScore;

    private int militaryScore;
    
    private int gold;
    
    private int score;

    /**
     * @return the player
     */
    public String getPlayer() {
        return player;
    }

    /**
     * @param player
     *            the player to set
     */
    public void setPlayer(String player) {
        this.player = player;
    }

    /**
     * @return the turnNo
     */
    public int getTurnNo() {
        return turnNo;
    }

    /**
     * @param turnNo
     *            the turnNo to set
     */
    public void setTurnNo(int turnNo) {
        this.turnNo = turnNo;
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
     * @return the gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * @param gold the gold to set
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
     * @param score the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }
}
