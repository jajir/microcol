package org.microcol.model.store;

/**
 * Saved entry of the persistent top ten (R-0197). Every component is boxed so a
 * missing value is rejected on load instead of becoming zero. The victory order
 * and declaration year are present only for a declared rebellion.
 */
public final class HallOfFameEntryPo {

    private String identity;

    /** Internal name of the nation; its identity, not the displayed name. */
    private String playerName;

    /** Name the player had when the result was recorded. */
    private String playerDisplayName;

    /** Difficulty name of the game the result was recorded in. */
    private String difficulty;

    private Integer servants;

    private Integer colonists;

    private Integer experts;

    private Integer fathers;

    private Long gold;

    private Integer supportPercent;

    private Integer destroyedSettlements;

    private Long bellsBeyondTarget;

    private Integer victoryOrder;

    private Integer declarationYear;

    private Long total;

    /**
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(final String identity) {
        this.identity = identity;
    }

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the playerDisplayName
     */
    public String getPlayerDisplayName() {
        return playerDisplayName;
    }

    /**
     * @param playerDisplayName the playerDisplayName to set
     */
    public void setPlayerDisplayName(final String playerDisplayName) {
        this.playerDisplayName = playerDisplayName;
    }

    /**
     * @return the difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * @param difficulty the difficulty to set
     */
    public void setDifficulty(final String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * @return the servants
     */
    public Integer getServants() {
        return servants;
    }

    /**
     * @param servants the servants to set
     */
    public void setServants(final Integer servants) {
        this.servants = servants;
    }

    /**
     * @return the colonists
     */
    public Integer getColonists() {
        return colonists;
    }

    /**
     * @param colonists the colonists to set
     */
    public void setColonists(final Integer colonists) {
        this.colonists = colonists;
    }

    /**
     * @return the experts
     */
    public Integer getExperts() {
        return experts;
    }

    /**
     * @param experts the experts to set
     */
    public void setExperts(final Integer experts) {
        this.experts = experts;
    }

    /**
     * @return the fathers
     */
    public Integer getFathers() {
        return fathers;
    }

    /**
     * @param fathers the fathers to set
     */
    public void setFathers(final Integer fathers) {
        this.fathers = fathers;
    }

    /**
     * @return the gold
     */
    public Long getGold() {
        return gold;
    }

    /**
     * @param gold the gold to set
     */
    public void setGold(final Long gold) {
        this.gold = gold;
    }

    /**
     * @return the supportPercent
     */
    public Integer getSupportPercent() {
        return supportPercent;
    }

    /**
     * @param supportPercent the supportPercent to set
     */
    public void setSupportPercent(final Integer supportPercent) {
        this.supportPercent = supportPercent;
    }

    /**
     * @return the destroyedSettlements
     */
    public Integer getDestroyedSettlements() {
        return destroyedSettlements;
    }

    /**
     * @param destroyedSettlements the destroyedSettlements to set
     */
    public void setDestroyedSettlements(final Integer destroyedSettlements) {
        this.destroyedSettlements = destroyedSettlements;
    }

    /**
     * @return the bellsBeyondTarget
     */
    public Long getBellsBeyondTarget() {
        return bellsBeyondTarget;
    }

    /**
     * @param bellsBeyondTarget the bellsBeyondTarget to set
     */
    public void setBellsBeyondTarget(final Long bellsBeyondTarget) {
        this.bellsBeyondTarget = bellsBeyondTarget;
    }

    /**
     * @return the victoryOrder
     */
    public Integer getVictoryOrder() {
        return victoryOrder;
    }

    /**
     * @param victoryOrder the victoryOrder to set
     */
    public void setVictoryOrder(final Integer victoryOrder) {
        this.victoryOrder = victoryOrder;
    }

    /**
     * @return the declarationYear
     */
    public Integer getDeclarationYear() {
        return declarationYear;
    }

    /**
     * @param declarationYear the declarationYear to set
     */
    public void setDeclarationYear(final Integer declarationYear) {
        this.declarationYear = declarationYear;
    }

    /**
     * @return the total
     */
    public Long getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(final Long total) {
        this.total = total;
    }

}
