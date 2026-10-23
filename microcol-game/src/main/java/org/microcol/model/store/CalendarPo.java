package org.microcol.model.store;

public final class CalendarPo {

    private int startYear;
    private int endYear;
    private int numberOfPlayedTurns;

    /**
     * Explicit free-play score policy (R-0193). A missing value means an
     * unscored game such as a mission; it is never inferred from the end year.
     */
    private boolean scoredPlay;

    /**
     * @return whether the free-play score policy applies
     */
    public boolean isScoredPlay() {
        return scoredPlay;
    }

    /**
     * @param scoredPlay whether the free-play score policy applies
     */
    public void setScoredPlay(final boolean scoredPlay) {
        this.scoredPlay = scoredPlay;
    }

    /**
     * @return the startYear
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * @param startYear the startYear to set
     */
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    /**
     * @return the endYear
     */
    public int getEndYear() {
        return endYear;
    }

    /**
     * @param endYear the endYear to set
     */
    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    /**
     * @return the numberOfPlayedTurns
     */
    public int getNumberOfPlayedTurns() {
        return numberOfPlayedTurns;
    }

    /**
     * @param numberOfPlayedTurns the numberOfPlayedTurns to set
     */
    public void setNumberOfPlayedTurns(int numberOfPlayedTurns) {
        this.numberOfPlayedTurns = numberOfPlayedTurns;
    }

}
